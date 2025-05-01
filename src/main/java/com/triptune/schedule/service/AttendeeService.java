package com.triptune.schedule.service;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendeeService {
    private static final int MAX_ATTENDEE_NUMBER = 5;

    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;


    public List<AttendeeResponse> getAttendeesByScheduleId(Long scheduleId) {
        return travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(scheduleId)
                .stream()
                .map(AttendeeResponse::from)
                .collect(Collectors.toList());
    }

    public void createAttendee(Long scheduleId, Long memberId, AttendeeRequest attendeeRequest) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        validateAttendeeAddition(scheduleId, memberId);

        Member guest = getMemberByEmail(attendeeRequest.getEmail());
        validateAttendeeAlreadyExists(scheduleId, guest.getMemberId());

        TravelAttendee travelAttendee = TravelAttendee.of(schedule, guest, attendeeRequest.getPermission());
        travelAttendeeRepository.save(travelAttendee);
    }

    public void validateAttendeeAddition(Long scheduleId, Long memberId){
        validateAttendeeCount(scheduleId);
        validateAuthor(scheduleId, memberId, ErrorCode.FORBIDDEN_SHARE_ATTENDEE);
    }

    private TravelSchedule getScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    public void validateAttendeeCount(Long scheduleId){
        int attendeeCnt = travelAttendeeRepository.countByTravelSchedule_ScheduleId(scheduleId);

        if(attendeeCnt >= MAX_ATTENDEE_NUMBER){
            throw new ConflictAttendeeException(ErrorCode.OVER_ATTENDEE_NUMBER);
        }
    }

    public void validateAuthor(Long scheduleId, Long memberId, ErrorCode errorCode){
        boolean isAuthor =  travelAttendeeRepository
                .existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(scheduleId, memberId, AttendeeRole.AUTHOR);

        if(!isAuthor){
            throw new ForbiddenAttendeeException(errorCode);
        }
    }

    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public void validateAttendeeAlreadyExists(Long scheduleId, Long memberId){
        boolean isExistedAttendee = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId);

        if (isExistedAttendee){
            throw new ConflictAttendeeException(ErrorCode.ALREADY_ATTENDEE);
        }
    }

    public void updateAttendeePermission(Long scheduleId, Long memberId, Long attendeeId, AttendeePermissionRequest attendeePermissionRequest) {
        validateAuthor(scheduleId, memberId, ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION);

        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(scheduleId, attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION);
        }

        attendee.updatePermission(attendeePermissionRequest.getPermission());
    }


    public void leaveAttendee(Long scheduleId, Long memberId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }


    public void removeAttendee(Long scheduleId, Long memberId, Long attendeeId) {
        validateAuthor(scheduleId, memberId, ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);

        TravelAttendee attendee = travelAttendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));

        if (attendee.getMember().getMemberId().equals(memberId)){
            throw new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
        }

        travelAttendeeRepository.delete(attendee);
    }
}
