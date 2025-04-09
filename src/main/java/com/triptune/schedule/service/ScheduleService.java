package com.triptune.schedule.service;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.dto.AuthorDTO;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.pagination.PageResponse;
import com.triptune.global.response.pagination.SchedulePageResponse;
import com.triptune.global.util.PageUtils;
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
    private final ChatMessageRepository chatMessageRepository;

    public SchedulePageResponse<ScheduleInfoResponse> getAllSchedulesByEmail(int page, String email) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<TravelSchedule> schedulePage = travelScheduleRepository.findTravelSchedulesByEmail(pageable, email);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulePage, email);
        int sharedScheduleCnt = travelScheduleRepository.countSharedTravelSchedulesByEmail(email);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtils.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.ofAll(scheduleInfoResponsePage, sharedScheduleCnt);
    }


    public SchedulePageResponse<ScheduleInfoResponse> getSharedSchedulesByEmail(int page, String email) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<TravelSchedule> schedulePage = travelScheduleRepository.findSharedTravelSchedulesByEmail(pageable, email);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulePage, email);
        int totalElements = travelScheduleRepository.countTravelSchedulesByEmail(email);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtils.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.ofShared(scheduleInfoResponsePage, totalElements);
    }


    public Page<OverviewScheduleResponse> getEnableEditScheduleByEmail(int page, String email) {
        Pageable pageable = PageUtils.scheduleModalPageable(page);

        return travelScheduleRepository.findEnableEditTravelSchedulesByEmail(pageable, email)
                .map(schedule -> {
                    String author = findAuthorNicknameByScheduleId(schedule.getScheduleId());
                    return OverviewScheduleResponse.from(schedule, author);
                });
    }

    public String findAuthorNicknameByScheduleId(Long scheduleId){
        return travelAttendeeRepository.findAuthorNicknameByScheduleId(scheduleId);
    }

    public SchedulePageResponse<ScheduleInfoResponse> searchAllSchedules(int page, String keyword, String email) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<TravelSchedule> schedulesPage = travelScheduleRepository.searchTravelSchedulesByEmailAndKeyword(pageable, keyword, email);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulesPage, email);
        int sharedElements = travelScheduleRepository.countSharedTravelSchedulesByEmailAndKeyword(keyword, email);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtils.createPage(scheduleInfoResponseList, pageable, schedulesPage.getTotalElements());
        return SchedulePageResponse.ofAll(scheduleInfoResponsePage, sharedElements);
    }



    public SchedulePageResponse<ScheduleInfoResponse> searchSharedSchedules(int page, String keyword, String email) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<TravelSchedule> schedulesPage = travelScheduleRepository.searchSharedTravelSchedulesByEmailAndKeyword(pageable, keyword, email);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulesPage, email);
        int totalElements = travelScheduleRepository.countTravelSchedulesByEmailAndKeyword(keyword, email);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtils.createPage(scheduleInfoResponseList, pageable, schedulesPage.getTotalElements());
        return SchedulePageResponse.ofShared(scheduleInfoResponsePage, totalElements);
    }


    public List<ScheduleInfoResponse> createScheduleInfoResponse(Page<TravelSchedule> schedulePage, String email){
        if (schedulePage.getContent().isEmpty()){
            return Collections.emptyList();
        }

        return schedulePage.stream()
                .map(schedule -> {
                    String thumbnailUrl = getThumbnailUrl(schedule);
                    TravelAttendee attendee = getAttendeeInfo(schedule, email);
                    AuthorDTO authorDTO = createAuthorDTO(schedule);

                    return ScheduleInfoResponse.from(schedule, attendee.getRole(), thumbnailUrl, authorDTO);
                })
                .collect(Collectors.toList());
    }


    public AuthorDTO createAuthorDTO(TravelSchedule schedule){
        Member author = schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getRole().equals(AttendeeRole.AUTHOR))
                .map(TravelAttendee::getMember)
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.AUTHOR_NOT_FOUND));

        return AuthorDTO.of(author.getNickname(), author.getProfileImage().getS3ObjectUrl());
    }


    public TravelAttendee getAttendeeInfo(TravelSchedule schedule, String email){
        return schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getMember().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

    }


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


    public ScheduleCreateResponse createSchedule(ScheduleCreateRequest scheduleCreateRequest, String email){
        TravelSchedule travelSchedule = TravelSchedule.from(scheduleCreateRequest);
        TravelSchedule savedTravelSchedule = travelScheduleRepository.save(travelSchedule);

        Member member = getMemberByEmail(email);

        TravelAttendee travelAttendee = TravelAttendee.of(savedTravelSchedule, member);
        travelAttendeeRepository.save(travelAttendee);

        return ScheduleCreateResponse.from(savedTravelSchedule);
    }


    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public ScheduleDetailResponse getScheduleDetail(Long scheduleId, int page) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);

        // 여행지 정보: Page<TravelPlace> -> PageResponse<TravelSimpleResponse> 로 변경
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceResponse> travelPlacesDTO = travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구");

        return ScheduleDetailResponse.from(schedule, PageResponse.of(travelPlacesDTO));
    }


    public void updateSchedule(String email, Long scheduleId, ScheduleUpdateRequest scheduleUpdateRequest) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        TravelAttendee attendee = getAttendeeInfo(schedule, email);
        checkScheduleEditPermission(attendee);

        schedule.set(scheduleUpdateRequest);
        updateTravelRouteInSchedule(schedule, scheduleUpdateRequest.getTravelRoute());
    }


    private TravelSchedule getScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    public void checkScheduleEditPermission(TravelAttendee attendee){
        if (!attendee.getPermission().isEnableEdit()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }
    }


    public void updateTravelRouteInSchedule(TravelSchedule schedule, List<RouteRequest> routeRequestList){
        travelRouteRepository.deleteAllByTravelSchedule_ScheduleId(schedule.getScheduleId());

        if (schedule.getTravelRouteList() == null){
            schedule.updateTravelRouteList(new ArrayList<>());
        } else{
            schedule.getTravelRouteList().clear();
        }

        if (routeRequestList != null && !routeRequestList.isEmpty()){
            for(RouteRequest routeRequest : routeRequestList){
                TravelPlace place = getPlaceByPlaceId(routeRequest.getPlaceId());
                TravelRoute route = TravelRoute.of(schedule, place, routeRequest.getRouteOrder());
                schedule.getTravelRouteList().add(route);
                travelRouteRepository.save(route);
            }
        }
    }


    private TravelPlace getPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findById(placeId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }


    public void deleteSchedule(Long scheduleId, String email) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_Email(scheduleId, email)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (!attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
        }

        travelScheduleRepository.deleteById(scheduleId);
        deleteChatMessageByScheduleId(scheduleId);
    }


    public void deleteChatMessageByScheduleId(Long scheduleId){
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(scheduleId);

        if (!chatMessages.isEmpty()){
            chatMessageRepository.deleteAllByScheduleId(scheduleId);
        }
    }


}


