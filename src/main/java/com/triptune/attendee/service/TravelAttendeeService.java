package com.triptune.attendee.service;

import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.attendee.dto.request.AttendeePermissionRequest;
import com.triptune.attendee.dto.request.AttendeeRequest;
import com.triptune.attendee.dto.response.AttendeeResponse;
import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.attendee.enums.AttendeeRole;
import com.triptune.global.websocket.event.ScheduleEvent;
import com.triptune.global.websocket.event.EventType;
import com.triptune.attendee.dto.response.AttendeeCreateEventResponse;
import com.triptune.attendee.dto.response.AttendeePermissionEventResponse;
import com.triptune.attendee.dto.response.AttendeeRemoveEventResponse;
import com.triptune.attendee.exception.ConflictAttendeeException;
import com.triptune.attendee.exception.ForbiddenAttendeeException;
import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TravelAttendeeService {
    private static final int MAX_ATTENDEE_NUMBER = 5;

    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ApplicationEventPublisher publisher;
    private final MemberRepository memberRepository;
    private final S3ObjectManager s3ObjectManager;


    public List<AttendeeResponse> getAttendeesByScheduleId(Long scheduleId) {
        List<TravelAttendee> attendees = travelAttendeeRepository.findAllByScheduleId(scheduleId);
        List<AttendeeResponse> response = new ArrayList<>();

        for (TravelAttendee attendee : attendees) {
            String profileUrl = s3ObjectManager.generateS3ObjectUrl(attendee.getMember().getProfileImage().getS3ObjectKey());
            AttendeeResponse attendeeRes = AttendeeResponse.of(attendee, profileUrl);
            response.add(attendeeRes);

        }

        return response;
    }

    @Transactional
    public void createAttendee(Long scheduleId, Long authorId, AttendeeRequest attendeeRequest) {
        validateAttendeeAddition(scheduleId, authorId);
        TravelSchedule schedule = getSchedule(scheduleId);

        Member guest = getMember(attendeeRequest.getEmail());
        validateAttendeeAlreadyExists(scheduleId, guest.getMemberId());

        TravelAttendee attendee = TravelAttendee.createGuest(schedule, guest, attendeeRequest.getPermission());
        travelAttendeeRepository.save(attendee);

        String profileUrl = getProfileUrl(guest.getMemberId());

        publisher.publishEvent(ScheduleEvent.withNickname(
                scheduleId,
                EventType.ATTENDEE_ADDED,
                guest.getNickname(),
                AttendeeCreateEventResponse.of(attendee, profileUrl)
            )
        );

    }


    private TravelSchedule getSchedule(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateAttendeeAddition(Long scheduleId, Long authorId){
        validateAttendeeCount(scheduleId);
        validateAuthor(scheduleId, authorId, ErrorCode.FORBIDDEN_SHARE_ATTENDEE);
    }

    private String getProfileUrl(Long memberId) {
        String s3ObjectKey = profileImageRepository.findS3ObjectKeyByMemberId(memberId);
        return s3ObjectManager.generateS3ObjectUrl(s3ObjectKey);
    }

    private void validateAttendeeCount(Long scheduleId){
        int attendeeCnt = travelAttendeeRepository.countByTravelSchedule_ScheduleId(scheduleId);

        if(attendeeCnt >= MAX_ATTENDEE_NUMBER){
            throw new ConflictAttendeeException(ErrorCode.OVER_ATTENDEE_NUMBER);
        }
    }


    private Member getMember(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    private void validateAttendeeAlreadyExists(Long scheduleId, Long memberId){
        boolean isExistedAttendee = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId);

        if (isExistedAttendee){
            throw new ConflictAttendeeException(ErrorCode.ALREADY_ATTENDEE);
        }
    }

    @Transactional
    public void updateAttendeePermission(AttendeePermissionRequest permissionRequest,
                                         Long scheduleId,
                                         Long authorId,
                                         Long attendeeId) {
        validateAuthor(scheduleId, authorId, ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION);

        TravelAttendee attendee = getAttendeeByScheduleIdAndAttendeeId(scheduleId, attendeeId);
        if (attendee.getRole().isAuthor()){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION);
        }

        attendee.updatePermission(permissionRequest.getPermission());

        publisher.publishEvent(ScheduleEvent.withPermission(
                scheduleId,
                EventType.ATTENDEE_PERMISSION_UPDATED,
                attendee.getMember().getNickname(),
                attendee.getPermission().getDisplayName(),
                AttendeePermissionEventResponse.from(attendee)
            )
        );
    }


    @Transactional
    public void leaveAttendee(Long scheduleId, Long memberId) {
        TravelAttendee attendee = getAttendeeByScheduleIdAndMemberId(scheduleId, memberId);

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
        }

        Long attendeeId = attendee.getAttendeeId();
        String nickname = attendee.getMember().getNickname();
        travelAttendeeRepository.deleteById(attendeeId);

        publisher.publishEvent(ScheduleEvent.withNickname(
                scheduleId,
                EventType.ATTENDEE_LEFT,
                nickname,
                new AttendeeRemoveEventResponse(attendeeId)
            )
        );
    }

    private TravelAttendee getAttendeeByScheduleIdAndMemberId(Long scheduleId, Long memberId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    @Transactional
    public void removeAttendee(Long scheduleId, Long authorId, Long attendeeId) {
        validateAuthor(scheduleId, authorId, ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);

        TravelAttendee attendee = getAttendeeByScheduleIdAndAttendeeId(scheduleId, attendeeId);
        validateAuthorCannotLeave(authorId, attendee);

        String nickname = attendee.getMember().getNickname();
        travelAttendeeRepository.delete(attendee);

        publisher.publishEvent(ScheduleEvent.withNickname(
                scheduleId,
                EventType.ATTENDEE_REMOVED,
                nickname,
                new AttendeeRemoveEventResponse(attendeeId)
            )
        );
    }


    private void validateAuthor(Long scheduleId, Long authorId, ErrorCode errorCode){
        boolean isAuthor =  travelAttendeeRepository
                .existsAttendee(scheduleId, authorId, AttendeeRole.AUTHOR);

        if(!isAuthor){
            throw new ForbiddenAttendeeException(errorCode);
        }
    }

    private TravelAttendee getAttendeeByScheduleIdAndAttendeeId(Long scheduleId, Long attendeeId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(scheduleId, attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));
    }


    private static void validateAuthorCannotLeave(Long authorId, TravelAttendee attendee) {
        if (attendee.getMember().getMemberId().equals(authorId)){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
        }
    }
}
