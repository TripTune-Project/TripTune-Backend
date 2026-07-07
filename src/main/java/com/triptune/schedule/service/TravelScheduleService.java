package com.triptune.schedule.service;

import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.schedule.service.dto.AuthorDTO;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.page.PageResponse;
import com.triptune.global.response.page.SchedulePageResponse;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import lombok.RequiredArgsConstructor;
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
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TravelRouteService travelRouteService;
    private final S3ObjectManager s3ObjectManager;

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


    public Page<OverviewScheduleResponse> getEnableEditSchedules(int page, Long memberId) {
        Pageable pageable = PageUtils.scheduleModalPageable(page);

        return travelScheduleRepository.findEnableEditTravelSchedules(pageable, memberId)
                .map(schedule -> {
                    String authorNickname = travelAttendeeRepository.findAuthorNicknameByScheduleId(schedule.getScheduleId());
                    return OverviewScheduleResponse.from(schedule, authorNickname);
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

        Member member = getMemberById(memberId);

        TravelAttendee attendee = TravelAttendee.createAuthor(schedule, member);
        travelAttendeeRepository.save(attendee);

        return ScheduleCreateResponse.from(schedule);
    }


    private Member getMemberById(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public ScheduleDetailResponse getScheduleDetail(Long scheduleId, int page) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);

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
    public void updateSchedule(ScheduleUpdateRequest scheduleUpdateRequest, Long memberId, Long scheduleId) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        TravelAttendee attendee = getAttendeeInfo(schedule, memberId);
        checkScheduleEditPermission(attendee);

        schedule.updateSchedule(scheduleUpdateRequest);
        travelRouteService.updateTravelRouteInSchedule(schedule, scheduleUpdateRequest.getTravelRoutes());
    }


    private TravelSchedule getScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private TravelAttendee getAttendeeInfo(TravelSchedule schedule, Long memberId){
        return schedule.getTravelAttendees().stream()
                .filter(attendee -> attendee.isSameMember(memberId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

    }


    private void checkScheduleEditPermission(TravelAttendee attendee){
        if (!attendee.getPermission().isEnableEdit()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }
    }


    @Transactional
    public void deleteSchedule(Long scheduleId, Long memberId) {
        TravelAttendee attendee = getAttendeeByScheduleIdAndMemberId(scheduleId, memberId);

        if (!attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
        }

        travelScheduleRepository.deleteById(scheduleId);
        deleteChatMessageByScheduleId(scheduleId);
    }

    private TravelAttendee getAttendeeByScheduleIdAndMemberId(Long scheduleId, Long memberId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    private void deleteChatMessageByScheduleId(Long scheduleId){
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(scheduleId);

        if (!chatMessages.isEmpty()){
            chatMessageRepository.deleteAllByScheduleId(scheduleId);
        }
    }


}


