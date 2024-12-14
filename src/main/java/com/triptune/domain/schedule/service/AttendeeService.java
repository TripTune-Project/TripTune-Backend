package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.domain.schedule.dto.request.AttendeeRequest;
import com.triptune.domain.schedule.dto.response.AttendeeResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ConflictAttendeeException;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
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
        List<TravelAttendee> travelAttendees = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(scheduleId);
        return travelAttendees.stream().map(AttendeeResponse::from).collect(Collectors.toList());
    }

    public void createAttendee(Long scheduleId, String userId, AttendeeRequest attendeeRequest) {
        // TODO 1. 일정 정보 가져옴 2. 참석자 5명 이상인지 확인 3. 요청자 작성자인지 확인 4. 이미 참석자인지 확인
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        validateAttendeeCount(scheduleId);
        validateAuthor(scheduleId, userId, ErrorCode.FORBIDDEN_SHARE_ATTENDEE);


        Member guest = getMemberByEmail(attendeeRequest.getEmail());
        validateAttendeeExists(scheduleId, guest);

        TravelAttendee travelAttendee = TravelAttendee.of(schedule, guest, attendeeRequest.getPermission());
        travelAttendeeRepository.save(travelAttendee);
    }

    public TravelSchedule getScheduleByScheduleId(Long scheduleId){
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

    public Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }


    public void validateAttendeeExists(Long scheduleId, Member guest){
        boolean isExistedAttendee = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, guest.getUserId());

        if (isExistedAttendee){
            throw new ConflictAttendeeException(ErrorCode.ALREADY_ATTENDEE);
        }
    }

    public void updateAttendeePermission(Long scheduleId, String userId, Long attendeeId, AttendeePermissionRequest attendeePermissionRequest) {
        // TODO : 1. 일정 존재하는지 2. 요청자가 일정 작성자가 맞는지 3. attendee 확인
        getScheduleByScheduleId(scheduleId);
        validateAuthor(scheduleId, userId, ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION);

        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(scheduleId, attendeeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_ATTENDEE_PERMISSION);
        }

        attendee.setPermission(attendeePermissionRequest.getPermission());
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
