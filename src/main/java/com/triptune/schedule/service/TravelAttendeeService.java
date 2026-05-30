package com.triptune.schedule.service;

import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.dto.response.AttendeeResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.exception.ConflictAttendeeException;
import com.triptune.schedule.exception.ForbiddenAttendeeException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import lombok.RequiredArgsConstructor;
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
    private final MemberRepository memberRepository;
    private final S3ObjectManager s3ObjectManager;


    public List<AttendeeResponse> getAttendeesByScheduleId(Long scheduleId) {
        List<TravelAttendee> travelAttendees = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(scheduleId);
        List<AttendeeResponse> response = new ArrayList<>();

        for (TravelAttendee travelAttendee : travelAttendees) {
            String profileUrl = s3ObjectManager.generateS3ObjectUrl(travelAttendee.getMember().getProfileImage().getS3ObjectKey());
            AttendeeResponse attendeeRes = AttendeeResponse.of(travelAttendee, profileUrl);
            response.add(attendeeRes);

        }

        return response;
    }

    @Transactional
    public void createAttendee(Long scheduleId, Long memberId, AttendeeRequest attendeeRequest) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        validateAttendeeAddition(scheduleId, memberId);

        Member guest = getMemberByEmail(attendeeRequest.getEmail());
        validateAttendeeAlreadyExists(scheduleId, guest.getMemberId());

        TravelAttendee travelAttendee = TravelAttendee.createGuest(schedule, guest, attendeeRequest.getPermission());
        travelAttendeeRepository.save(travelAttendee);
    }

    private TravelSchedule getScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateAttendeeAddition(Long scheduleId, Long memberId){
        validateAttendeeCount(scheduleId);
        validateAuthor(scheduleId, memberId, ErrorCode.FORBIDDEN_SHARE_ATTENDEE);
    }

    private void validateAttendeeCount(Long scheduleId){
        int attendeeCnt = travelAttendeeRepository.countByTravelSchedule_ScheduleId(scheduleId);

        if(attendeeCnt >= MAX_ATTENDEE_NUMBER){
            throw new ConflictAttendeeException(ErrorCode.OVER_ATTENDEE_NUMBER);
        }
    }


    private Member getMemberByEmail(String email){
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
    public void updateAttendeePermission(AttendeePermissionRequest attendeePermissionRequest,
                                         Long scheduleId,
                                         Long memberId,
                                         Long attendeeId) {
        validateAuthor(scheduleId, memberId, ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION);

        TravelAttendee attendee = getAttendeeByScheduleIdAndAttendeeId(scheduleId, attendeeId);

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION);
        }

        attendee.updatePermission(attendeePermissionRequest.getPermission());
    }

    private TravelAttendee getAttendeeByScheduleIdAndAttendeeId(Long scheduleId, Long attendeeId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(scheduleId, attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));
    }

    @Transactional
    public void leaveAttendee(Long scheduleId, Long memberId) {
        TravelAttendee attendee = getAttendeeByScheduleIdAndMemberId(scheduleId, memberId);

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }

    private TravelAttendee getAttendeeByScheduleIdAndMemberId(Long scheduleId, Long memberId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    @Transactional
    public void removeAttendee(Long scheduleId, Long memberId, Long attendeeId) {
        validateAuthor(scheduleId, memberId, ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);

        TravelAttendee attendee = getAttendeeById(attendeeId);

        if (attendee.getMember().getMemberId().equals(memberId)){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
        }

        travelAttendeeRepository.delete(attendee);
    }

    private TravelAttendee getAttendeeById(Long attendeeId) {
        return travelAttendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));
    }


    private void validateAuthor(Long scheduleId, Long memberId, ErrorCode errorCode){
        boolean isAuthor =  travelAttendeeRepository
                .existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(scheduleId, memberId, AttendeeRole.AUTHOR);

        if(!isAuthor){
            throw new ForbiddenAttendeeException(errorCode);
        }
    }
}
