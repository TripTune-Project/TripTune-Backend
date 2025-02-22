package com.triptune.schedule.service;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.dto.response.AttendeeResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.exception.ConflictAttendeeException;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
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

    public void createAttendee(Long scheduleId, String userId, AttendeeRequest attendeeRequest) {
        TravelSchedule schedule = findScheduleByScheduleId(scheduleId);
        validateAttendeeAddition(scheduleId, userId);

        Member guest = findMemberByEmail(attendeeRequest.getEmail());
        validateAttendeeAlreadyExists(scheduleId, guest.getUserId());

        TravelAttendee travelAttendee = TravelAttendee.of(schedule, guest, attendeeRequest.getPermission());
        travelAttendeeRepository.save(travelAttendee);
    }

    public void validateAttendeeAddition(Long scheduleId, String userId){
        validateAttendeeCount(scheduleId);
        validateAuthor(scheduleId, userId, ErrorCode.FORBIDDEN_SHARE_ATTENDEE);
    }

    public TravelSchedule findScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    public void validateAttendeeCount(Long scheduleId){
        int attendeeCnt = travelAttendeeRepository.countByTravelSchedule_ScheduleId(scheduleId);

        if(attendeeCnt >= MAX_ATTENDEE_NUMBER){
            throw new ConflictAttendeeException(ErrorCode.OVER_ATTENDEE_NUMBER);
        }
    }

    public void validateAuthor(Long scheduleId, String userId, ErrorCode errorCode){
        boolean isAuthor =  travelAttendeeRepository
                .existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(scheduleId, userId, AttendeeRole.AUTHOR);

        if(!isAuthor){
            throw new ForbiddenScheduleException(errorCode);
        }
    }

    public Member findMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public void validateAttendeeAlreadyExists(Long scheduleId, String userId){
        boolean isExistedAttendee = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId);

        if (isExistedAttendee){
            throw new ConflictAttendeeException(ErrorCode.ALREADY_ATTENDEE);
        }
    }

    public void updateAttendeePermission(Long scheduleId, String userId, Long attendeeId, AttendeePermissionRequest attendeePermissionRequest) {
        validateAuthor(scheduleId, userId, ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION);

        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(scheduleId, attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_ATTENDEE_PERMISSION);
        }

        attendee.updatePermission(attendeePermissionRequest.getPermission());
    }


    public void leaveScheduleAsGuest(Long scheduleId, String userId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_REMOVE_AUTHOR_ATTENDEE);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }


}
