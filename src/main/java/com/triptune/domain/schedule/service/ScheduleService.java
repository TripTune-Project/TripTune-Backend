package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.AuthorDTO;
import com.triptune.domain.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.domain.schedule.dto.request.RouteRequest;
import com.triptune.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.domain.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.domain.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
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
    private final ChatMessageRepository chatMessageRepository;

    public SchedulePageResponse<ScheduleInfoResponse> getAllSchedulesByUserId(int page, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Page<TravelSchedule> schedulePage = travelScheduleRepository.findTravelSchedulesByUserId(pageable, userId);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulePage, userId);
        int sharedScheduleCnt = travelScheduleRepository.countSharedTravelSchedulesByUserId(userId);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.ofAll(scheduleInfoResponsePage, sharedScheduleCnt);
    }


    public SchedulePageResponse<ScheduleInfoResponse> getSharedSchedulesByUserId(int page, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Page<TravelSchedule> schedulePage = travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, userId);

        List<ScheduleInfoResponse> scheduleInfoResponseList = createScheduleInfoResponse(schedulePage, userId);
        int totalElements = travelScheduleRepository.countTravelSchedulesByUserId(userId);

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());
        return SchedulePageResponse.ofShared(scheduleInfoResponsePage, totalElements);
    }


    public Page<OverviewScheduleResponse> getEnableEditScheduleByUserId(int page, String userId) {
        Pageable pageable = PageUtil.scheduleModalPageable(page);

        return travelScheduleRepository.findEnableEditTravelSchedulesByUserId(pageable, userId)
                .map(schedule -> {
                    String author = findAuthorNicknameByScheduleId(schedule.getScheduleId());
                    return OverviewScheduleResponse.from(schedule, author);
                });
    }

    public String findAuthorNicknameByScheduleId(Long scheduleId){
        return travelAttendeeRepository.findAuthorNicknameByScheduleId(scheduleId);
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


    public AuthorDTO createAuthorDTO(TravelSchedule schedule){
        Member author = schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getRole().equals(AttendeeRole.AUTHOR))
                .map(TravelAttendee::getMember)
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.AUTHOR_NOT_FOUND));

        return AuthorDTO.of(author.getNickname(), author.getProfileImage().getS3ObjectUrl());
    }


    public TravelAttendee getAttendeeInfo(TravelSchedule schedule, String userId){
        return schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getMember().getUserId().equals(userId))
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


    public ScheduleCreateResponse createSchedule(ScheduleCreateRequest scheduleCreateRequest, String userId){
        TravelSchedule travelSchedule = TravelSchedule.from(scheduleCreateRequest);
        TravelSchedule savedTravelSchedule = travelScheduleRepository.save(travelSchedule);

        Member member = getMemberByUserId(userId);

        TravelAttendee travelAttendee = TravelAttendee.of(savedTravelSchedule, member);
        travelAttendeeRepository.save(travelAttendee);

        return ScheduleCreateResponse.from(savedTravelSchedule);
    }


    public Member getMemberByUserId(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }


    public ScheduleDetailResponse getScheduleDetail(Long scheduleId, int page) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);

        // 여행지 정보: Page<TravelPlace> -> PageResponse<TravelSimpleResponse> 로 변경
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<PlaceResponse> travelPlacesDTO = travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구")
                .map(PlaceResponse::from);

        return ScheduleDetailResponse.from(schedule, PageResponse.of(travelPlacesDTO));
    }


    public void updateSchedule(String userId, Long scheduleId, ScheduleUpdateRequest scheduleUpdateRequest) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        TravelAttendee attendee = getAttendeeInfo(schedule, userId);
        checkScheduleEditPermission(attendee);

        schedule.set(scheduleUpdateRequest);
        updateTravelRouteInSchedule(schedule, scheduleUpdateRequest.getTravelRoute());
    }


    public TravelSchedule getScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
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
            schedule.setTravelRouteList(new ArrayList<>());
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


    public TravelPlace getPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }


    public void deleteSchedule(Long scheduleId, String userId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
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


