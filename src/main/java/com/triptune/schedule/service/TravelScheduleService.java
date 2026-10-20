package com.triptune.schedule.service;

import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.route.repository.TravelRouteRepository;
import com.triptune.route.service.TravelRouteService;
import com.triptune.schedule.dto.response.*;
import com.triptune.schedule.event.ScheduleEvent;
import com.triptune.schedule.event.ScheduleEventListener;
import com.triptune.schedule.event.ScheduleEventType;
import com.triptune.schedule.event.dto.ScheduleUpdateEventResponse;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.attendee.service.dto.AuthorDTO;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.chat.entity.ChatMessage;
import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.chat.repository.ChatMessageRepository;
import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.page.PageResponse;
import com.triptune.global.response.page.SchedulePageResponse;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TravelScheduleService {

    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;
    private final TravelRouteRepository travelRouteRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final S3ObjectManager s3ObjectManager;
    private final ApplicationEventPublisher publisher;

    public SchedulePageResponse<ScheduleInfoResponse> getAllSchedules(int page, Long memberId) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<ScheduleInfoQueryDto> schedulePage = travelScheduleRepository.findTravelSchedules(pageable, memberId);
        Page<ScheduleInfoResponse> pageResult = toScheduleInfoPage(schedulePage);

        int sharedScheduleCnt = travelScheduleRepository.countSharedTravelSchedules(memberId);

        return SchedulePageResponse.ofAllSchedules(pageResult, sharedScheduleCnt);
    }


    public SchedulePageResponse<ScheduleInfoResponse> getSharedSchedules(int page, Long memberId) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<ScheduleInfoQueryDto> schedulePage = travelScheduleRepository.findSharedTravelSchedules(pageable, memberId);
        Page<ScheduleInfoResponse> pageResult = toScheduleInfoPage(schedulePage);

        int totalScheduleCnt = travelScheduleRepository.countTravelSchedules(memberId);

        return SchedulePageResponse.ofSharedSchedules(pageResult, totalScheduleCnt);
    }


    public Page<ScheduleOverviewResponse> getEnableEditSchedules(int page, Long memberId) {
        Pageable pageable = PageUtils.scheduleModalPageable(page);

        return travelScheduleRepository.findEnableEditTravelSchedules(pageable, memberId)
                .map(schedule -> {
                    String authorNickname = travelAttendeeRepository.findAuthorNicknameByScheduleId(schedule.getScheduleId());
                    return ScheduleOverviewResponse.from(schedule, authorNickname);
                });
    }


    public SchedulePageResponse<ScheduleInfoResponse> searchAllSchedules(int page, String keyword, Long memberId) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<ScheduleInfoQueryDto> schedulePage = travelScheduleRepository.searchTravelSchedules(pageable, keyword, memberId);
        Page<ScheduleInfoResponse> pageResult = toScheduleInfoPage(schedulePage);

        int totalScheduleCnt = travelScheduleRepository.countTravelSchedules(memberId);
        int sharedScheduleCnt = travelScheduleRepository.countSharedTravelSchedules(memberId);

        return SchedulePageResponse.of(pageResult, totalScheduleCnt, sharedScheduleCnt);
    }


    public SchedulePageResponse<ScheduleInfoResponse> searchSharedSchedules(int page, String keyword, Long memberId) {
        Pageable pageable = PageUtils.schedulePageable(page);
        Page<ScheduleInfoQueryDto> schedulePage = travelScheduleRepository.searchSharedTravelSchedules(pageable, keyword, memberId);
        Page<ScheduleInfoResponse> pageResult = toScheduleInfoPage(schedulePage);

        int totalScheduleCnt = travelScheduleRepository.countTravelSchedules(memberId);
        int sharedScheduleCnt = travelScheduleRepository.countSharedTravelSchedules(memberId);

        return SchedulePageResponse.of(pageResult, totalScheduleCnt, sharedScheduleCnt);
    }


    private Page<ScheduleInfoResponse> toScheduleInfoPage(Page<ScheduleInfoQueryDto> schedulePage){
        List<ScheduleInfoResponse> scheduleInfoResponses = schedulePage.stream()
                .map(this::toScheduleInfoResponse)
                .collect(Collectors.toList());

        return PageUtils.createPage(scheduleInfoResponses, schedulePage.getPageable(), schedulePage.getTotalElements());
    }

    private ScheduleInfoResponse toScheduleInfoResponse(ScheduleInfoQueryDto schedule){
        String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(schedule.getThumbnailS3ObjectKey());
        String authorProfileUrl = s3ObjectManager.generateS3ObjectUrl(schedule.getAuthorS3ObjectKey());
        AuthorDTO authorDTO = AuthorDTO.of(schedule.getAuthorNickname(), authorProfileUrl);

        return ScheduleInfoResponse.of(schedule, thumbnailUrl, authorDTO);
    }


    @Transactional
    public ScheduleCreateResponse createSchedule(ScheduleCreateRequest scheduleCreateRequest, Long memberId){
        TravelSchedule schedule = TravelSchedule.createTravelSchedule(
                scheduleCreateRequest.getScheduleName(),
                scheduleCreateRequest.getStartDate(),
                scheduleCreateRequest.getEndDate()
        );
        travelScheduleRepository.save(schedule);

        Member member = getMember(memberId);

        TravelAttendee attendee = TravelAttendee.createAuthor(schedule, member);
        travelAttendeeRepository.save(attendee);

        return ScheduleCreateResponse.from(schedule);
    }


    private Member getMember(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public ScheduleDetailResponse getScheduleDetail(Long scheduleId, int page) {
        TravelSchedule schedule = getSchedule(scheduleId);

        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceQueryDto> placePage = travelPlaceRepository.findNearbyTravelPlacesFromJungGu(pageable);

        List<PlaceResponse> placeResponses = placePage.getContent().stream()
                .map(place -> {
                    String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(place.getThumbnailS3ObjectKey());
                    return PlaceResponse.of(place, thumbnailUrl);
                }).toList();

        Page<PlaceResponse> responsePage = PageUtils.createPage(placeResponses, placePage.getPageable(), placePage.getTotalElements());
        return ScheduleDetailResponse.from(schedule, PageResponse.of(responsePage));
    }


    @Transactional
    public ScheduleUpdateResponse updateSchedule(ScheduleUpdateRequest scheduleUpdateRequest, Long memberId, Long scheduleId) {
        TravelSchedule schedule = getSchedule(scheduleId);
        TravelAttendee attendee = getAttendeeInfo(schedule, memberId);
        validateEditPermission(attendee);

        schedule.updateSchedule(
                scheduleUpdateRequest.getScheduleName(),
                scheduleUpdateRequest.getStartDate(),
                scheduleUpdateRequest.getEndDate()
        );

        schedule.increaseScheduleVersion();

        publisher.publishEvent(ScheduleEvent.of(
                scheduleId,
                ScheduleEventType.SCHEDULE_UPDATED,
                ScheduleUpdateEventResponse.from(schedule)
        ));

        return ScheduleUpdateResponse.of(schedule.getScheduleVersion());
    }


    private TravelSchedule getSchedule(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private TravelAttendee getAttendeeInfo(TravelSchedule schedule, Long memberId){
        return schedule.getTravelAttendees().stream()
                .filter(attendee -> attendee.isSameMember(memberId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

    }


    private void validateEditPermission(TravelAttendee attendee){
        if (!attendee.getPermission().isEnableEdit()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }
    }


    @Transactional
    public void deleteSchedule(Long scheduleId, Long memberId) {
        TravelAttendee attendee = getAttendee(scheduleId, memberId);

        if (!attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
        }

        travelAttendeeRepository.deleteAllByScheduleId(scheduleId);
        travelRouteRepository.deleteAllByScheduleId(scheduleId);
        travelScheduleRepository.deleteById(scheduleId);
        deleteChatMessages(scheduleId);
    }

    private TravelAttendee getAttendee(Long scheduleId, Long memberId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    private void deleteChatMessages(Long scheduleId){
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(scheduleId);

        if (!chatMessages.isEmpty()){
            chatMessageRepository.deleteAllByScheduleId(scheduleId);
        }
    }


}


