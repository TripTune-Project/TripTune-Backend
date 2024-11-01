package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.File;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.*;
import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.request.RouteRequest;
import com.triptune.domain.schedule.dto.request.UpdateScheduleRequest;
import com.triptune.domain.schedule.dto.response.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.pagination.PageResponse;
import com.triptune.global.response.pagination.SchedulePageResponse;
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {

    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final TravelRouteRepository travelRouteRepository;


    /**
     * 일정 목록 조회
     * @param page: 페이지 수
     * @param userId: 접속 사용자 아이디
     * @return SchedulePageResponse<ScheduleInfoResponse>: 사용자가 참석자로 포함되어있는 일정 목록들과 전제 일정 갯수, 공유된 일정 갯수가 포함된 dto
     */
    public SchedulePageResponse<ScheduleInfoResponse> getSchedules(int page, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Member member = getSavedMember(userId);

        Page<TravelSchedule> schedulePage = travelScheduleRepository.findTravelSchedulesByAttendee(pageable, member.getMemberId());

        List<ScheduleInfoResponse> scheduleInfoResponseList = new ArrayList<>();
        long sharedScheduleCnt = 0;

        if (!schedulePage.getContent().isEmpty()){
            scheduleInfoResponseList = schedulePage.stream()
                    .map(schedule -> convertToScheduleInfoResponse(member, schedule))
                    .collect(Collectors.toList());

            sharedScheduleCnt = schedulePage.getContent().stream()
                    .filter(schedule -> schedule.getTravelAttendeeList().size() > 1)
                    .count();
        }

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.of(scheduleInfoResponsePage, sharedScheduleCnt);
    }


    /**
     * TravelSchedule 엔티티를 ScheduleInfoResponse 로 변경
     * @param schedule: 일정
     * @return ScheduleInfoResponse: 대략적인 일정 정보가 포함된 dto
     */
    public ScheduleInfoResponse convertToScheduleInfoResponse(Member member, TravelSchedule schedule){
        String thumbnailUrl = getThumbnailUrl(schedule);
        TravelAttendee attendee = getAttendeeInfo(member, schedule);
        AuthorDTO authorDTO = getAuthorDTO(schedule);

        return ScheduleInfoResponse.entityToDto(schedule, attendee.getRole(), thumbnailUrl, authorDTO);
    }

    /**
     * 일정의 작성자 dto 생성 메소드
     * @param schedule: 일정
     * @return AuthorDTO: 작성자 정보 포함된 dto
     */
    public AuthorDTO getAuthorDTO(TravelSchedule schedule){
        Member author = getAuthorMember(schedule.getTravelAttendeeList());
        return AuthorDTO.of(author.getUserId(), author.getProfileImage().getS3ObjectUrl());
    }

    /**
     * 일정 참가자 중 작성자 정보 조회
     * @param attendeeList: 참가자 목록
     * @return Member: 작성자 entity
     */
    public Member getAuthorMember(List<TravelAttendee> attendeeList){
        return attendeeList.stream()
                .filter(attendee -> attendee.getRole().equals(AttendeeRole.AUTHOR))
                .map(TravelAttendee::getMember)
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.AUTHOR_NOT_FOUND));
    }


    /**
     * 일정 참석자 정보 조회
     * @param member: 사용자 객체
     * @param schedule: 일정 객체
     * @return TravelAttendee: 조회한 사용자의 일정 권한 및 허용 범위 포함된 객체
     */
    public TravelAttendee getAttendeeInfo(Member member, TravelSchedule schedule){
        return schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getMember().getMemberId().equals(member.getMemberId()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

    }

    /**
     * 썸네일 이미지 조회
     * @param schedule: 일정
     * @return String: 썸네일 url
     */
    public String getThumbnailUrl(TravelSchedule schedule){
        String thumbnailUrl = null;

        List<TravelRoute> travelRouteList = schedule.getTravelRouteList();

        if (travelRouteList != null && !travelRouteList.isEmpty()){
            TravelRoute route = schedule.getTravelRouteList().stream()
                    .filter(travelRoute -> travelRoute.getRouteOrder() == 1)
                    .findFirst()
                    .orElse(null);

            if (route != null){
                thumbnailUrl = File.getThumbnailUrl(route.getTravelPlace().getTravelImageList());
            }
        }

        return thumbnailUrl;
    }


    /**
     * 일정 생성
     * @param createScheduleRequest: 일정명, 날짜 등 포함된 dto
     * @param userId: 사용자 아이디
     * @return CreateScheduleResponse: scheduleId로 구성된 dto
     */
    public CreateScheduleResponse createSchedule(CreateScheduleRequest createScheduleRequest, String userId){
        TravelSchedule travelSchedule = TravelSchedule.of(createScheduleRequest);
        TravelSchedule savedTravelSchedule = travelScheduleRepository.save(travelSchedule);

        Member member = getSavedMember(userId);

        TravelAttendee travelAttendee = TravelAttendee.builder()
                .travelSchedule(savedTravelSchedule)
                .member(member)
                .role(AttendeeRole.AUTHOR)
                .permission(AttendeePermission.ALL)
                .build();

        travelAttendeeRepository.save(travelAttendee);

        return CreateScheduleResponse.entityToDto(savedTravelSchedule);
    }

    /**
     * 일정 상세 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return ScheduleResponse: 일정 정보, 여행지 정보(중구)로 구성된 dto
     */
    public ScheduleDetailResponse getScheduleDetail(Long scheduleId, int page) {
        TravelSchedule schedule = getSavedSchedule(scheduleId);

        // 여행지 정보: Page<TravelPlace> -> PageResponse<TravelSimpleResponse> 로 변경
        Page<PlaceResponse> travelPlacesDTO = getSimpleTravelPlacesByJunggu(page);
        PageResponse<PlaceResponse> placeDTOList = PageResponse.of(travelPlacesDTO);

        List<AttendeeDTO> attendeeDTOList = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId())
                .stream()
                .map(AttendeeDTO::entityToDTO)
                .toList();

        return ScheduleDetailResponse.entityToDTO(schedule, placeDTOList, attendeeDTOList);
    }


    /**
     * 일정 수정
     * @param userId: 사용자 아이디
     * @param scheduleId: 일정 인덱스
     * @param updateScheduleRequest: 수정 일정 dto
     */
    public void updateSchedule(String userId, Long scheduleId, UpdateScheduleRequest updateScheduleRequest) {
        TravelSchedule schedule = getSavedSchedule(scheduleId);
        Member member = getSavedMember(userId);
        TravelAttendee attendee = getAttendeeInfo(member, schedule);
        checkUserPermission(attendee.getPermission());

        schedule.set(updateScheduleRequest);
        updateTravelRouteInSchedule(schedule, updateScheduleRequest.getTravelRoute());
    }

    /**
     * 일정 수정 중 여행 루트 수정
     * @param schedule: 일정 객체
     * @param routeRequestList: 수정할 여행 루트
     */
    public void updateTravelRouteInSchedule(TravelSchedule schedule, List<RouteRequest> routeRequestList){
        travelRouteRepository.deleteAllByTravelSchedule_ScheduleId(schedule.getScheduleId());

        if (schedule.getTravelRouteList() == null) {
            schedule.setTravelRouteList(new ArrayList<>());
        } else{
            schedule.getTravelRouteList().clear();
        }

        if (routeRequestList != null && !routeRequestList.isEmpty()){
            for(RouteRequest routeRequest : routeRequestList){
                TravelPlace place = getSavedPlace(routeRequest.getPlaceId());
                TravelRoute route = TravelRoute.of(schedule, place, routeRequest.getRouteOrder());
                schedule.getTravelRouteList().add(route);
                travelRouteRepository.save(route);
            }
        }
    }



    /**
     * 사용자의 일정 허용 범위 체크
     * @param permission: 허용 범위
     */
    public void checkUserPermission(AttendeePermission permission){
        if (permission.equals(AttendeePermission.CHAT) || permission.equals(AttendeePermission.READ)){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }
    }


    public void deleteSchedule(Long scheduleId, String userId) {
        TravelSchedule schedule = getSavedSchedule(scheduleId);
        Member member = getSavedMember(userId);
        TravelAttendee attendee = getAttendeeInfo(member, schedule);

        if (!attendee.getRole().equals(AttendeeRole.AUTHOR)){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
        }

        travelScheduleRepository.deleteById(scheduleId);
    }

    /**
     * 여행지 정보 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return Page<PlaceSimpleResponse>: 중구 기준 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> getTravelPlaces(Long scheduleId, int page) {
        getSavedSchedule(scheduleId);
        return getSimpleTravelPlacesByJunggu(page);
    }


    /**
     * 여행지 검색
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @param keyword: 검색 키워드
     * @return Page<PlaceSimpleResponse>: 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> searchTravelPlaces(Long scheduleId, int page, String keyword) {
        getSavedSchedule(scheduleId);

        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelPlace> travelPlaces = travelPlaceRepository.searchTravelPlaces(pageable, keyword);

        return travelPlaces.map(PlaceResponse::entityToDto);
    }

    /**
     * 여행 루트 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return Page<RouteResponse>: 여행 루트 정보로 구성된 페이지 dto
     */
    public Page<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        getSavedSchedule(scheduleId);

        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelRoutes.map(t -> RouteResponse.entityToDto(t, t.getTravelPlace()));
    }


    /**
     * 중구 여행지 조회
     * @param page: 페이지 수
     * @return Page<PlaceSimpleResponse>: 중구 기준 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> getSimpleTravelPlacesByJunggu(int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelPlace> travelPlaces = travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구");

        return travelPlaces.map(PlaceResponse::entityToDto);
    }

    /**
     * 저장된 사용자 정보 조회
     * @param userId: 사용자 아이디
     * @return Member: 사용자 entity
     */
    public Member getSavedMember(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 저장된 일정 조회
     * @param scheduleId: 일정 인덱스
     * @return TravelSchedule: 일정 entity
     */
    public TravelSchedule getSavedSchedule(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    /**
     * 저장된 여행지 조회
     * @param placeId: 여행지 인덱스
     * @return TravelPlace: 여행지 entity
     */
    public TravelPlace getSavedPlace(Long placeId){
        return travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }


}


