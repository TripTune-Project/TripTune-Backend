package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.AttendeeDTO;
import com.triptune.domain.schedule.dto.AuthorDTO;
import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.request.RouteRequest;
import com.triptune.domain.schedule.dto.request.UpdateScheduleRequest;
import com.triptune.domain.schedule.dto.response.CreateScheduleResponse;
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
import com.triptune.domain.travel.repository.TravelPlaceRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {

    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final TravelRouteRepository travelRouteRepository;


    /**
     * 일정 목록 조회
     * @param page: 페이지 수
     * @param userId: 접속 사용자 아이디
     * @return SchedulePageResponse<ScheduleInfoResponse>: 사용자가 참석자로 포함되어있는 일정 목록들과 전제 일정 갯수, 공유된 일정 갯수가 포함된 dto
     */
    public SchedulePageResponse<ScheduleInfoResponse> getSchedules(int page, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Page<TravelSchedule> schedulePage = travelScheduleRepository.findTravelSchedulesByUserId(pageable, userId);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulePage, userId);
        int sharedScheduleCnt = travelScheduleRepository.countSharedTravelSchedulesByUserId(userId);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.ofAll(scheduleInfoResponsePage, sharedScheduleCnt);
    }


    public SchedulePageResponse<ScheduleInfoResponse> getSharedSchedules(int page, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Page<TravelSchedule> schedulePage = travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, userId);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulePage, userId);
        int totalElements = travelScheduleRepository.countTravelSchedulesByUserId(userId);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.ofShared(scheduleInfoResponsePage, totalElements);
    }


    public SchedulePageResponse<ScheduleInfoResponse> searchAllSchedules(int page, String keyword, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Page<TravelSchedule> schedulesPage = travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, keyword, userId);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulesPage, userId);
        int sharedElements = travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword(keyword, userId);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulesPage.getTotalElements());
        return SchedulePageResponse.ofAll(scheduleInfoResponsePage, sharedElements);
    }

    public SchedulePageResponse<ScheduleInfoResponse> searchSharedSchedules(int page, String keyword, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Page<TravelSchedule> schedulesPage = travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, keyword, userId);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulesPage, userId);
        int totalElements = travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword(keyword, userId);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulesPage.getTotalElements());
        return SchedulePageResponse.ofShared(scheduleInfoResponsePage, totalElements);
    }


    public List<ScheduleInfoResponse> createScheduleInfoResponse(Page<TravelSchedule> schedulePage, String userId){
        if (schedulePage.getContent().isEmpty()){
            return Collections.emptyList();
        }

        return schedulePage.stream()
                .map(schedule -> {
                    String thumbnailUrl = getThumbnailUrl(schedule);
                    TravelAttendee attendee = getAttendeeInfo(schedule, userId);
                    AuthorDTO authorDTO = createAuthorDTO(schedule);

                    return ScheduleInfoResponse.from(schedule, attendee.getRole(), thumbnailUrl, authorDTO);
                })
                .collect(Collectors.toList());
    }


    /**
     * 일정의 작성자 dto 생성 메소드
     * @param schedule: 일정
     * @return AuthorDTO: 작성자 정보 포함된 dto
     */
    public AuthorDTO createAuthorDTO(TravelSchedule schedule){
        Member author = schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getRole().equals(AttendeeRole.AUTHOR))
                .map(TravelAttendee::getMember)
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.AUTHOR_NOT_FOUND));

        return AuthorDTO.of(author.getUserId(), author.getProfileImage().getS3ObjectUrl());
    }

    /**
     * 일정 참석자 정보 조회
     * @param schedule: 일정 객체
     * @param userId: 사용자 아이디
     * @return TravelAttendee: 조회한 사용자의 일정 권한 및 허용 범위 포함된 객체
     */
    public TravelAttendee getAttendeeInfo(TravelSchedule schedule, String userId){
        return schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getMember().getUserId().equals(userId))
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
                thumbnailUrl = route.getTravelPlace().getThumbnailUrl();
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
        TravelSchedule travelSchedule = TravelSchedule.from(createScheduleRequest);
        TravelSchedule savedTravelSchedule = travelScheduleRepository.save(travelSchedule);

        Member member = getSavedMember(userId);

        TravelAttendee travelAttendee = TravelAttendee.of(savedTravelSchedule, member);
        travelAttendeeRepository.save(travelAttendee);

        return CreateScheduleResponse.from(savedTravelSchedule);
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
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<PlaceResponse> travelPlacesDTO = travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구")
                .map(PlaceResponse::from);

        return ScheduleDetailResponse.from(schedule, PageResponse.of(travelPlacesDTO));
    }


    /**
     * 일정 수정
     * @param userId: 사용자 아이디
     * @param scheduleId: 일정 인덱스
     * @param updateScheduleRequest: 수정 일정 dto
     */
    public void updateSchedule(String userId, Long scheduleId, UpdateScheduleRequest updateScheduleRequest) {
        TravelSchedule schedule = getSavedSchedule(scheduleId);
        TravelAttendee attendee = getAttendeeInfo(schedule, userId);
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

    /**
     * 일정 삭제
     * @param scheduleId: 일정 인덱스
     * @param userId: 사용자 아이디
     */
    public void deleteSchedule(Long scheduleId, String userId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (!attendee.isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
        }

        travelScheduleRepository.deleteById(scheduleId);
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


