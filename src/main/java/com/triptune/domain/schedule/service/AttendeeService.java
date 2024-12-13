package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.request.CreateAttendeeRequest;
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

    public void createAttendee(Long scheduleId, String userId, CreateAttendeeRequest createAttendeeRequest) {
        TravelSchedule schedule = getScheduleByScheduleId(scheduleId);
        validateAttendeeCount(scheduleId);
        validateAuthorPermission(scheduleId, userId);

        Member guest = getMemberByEmail(createAttendeeRequest.getEmail());
        validateNotAlreadyAttendee(scheduleId, guest);

        TravelAttendee travelAttendee = TravelAttendee.of(schedule, guest, createAttendeeRequest.getPermission());
        travelAttendeeRepository.save(travelAttendee);
    }

    private TravelSchedule getScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateAttendeeCount(Long scheduleId){
        int attendeeCnt = travelAttendeeRepository.countByTravelSchedule_ScheduleId(scheduleId);

        if(attendeeCnt >= MAX_ATTENDEE_NUMBER){
            throw new ConflictAttendeeException(ErrorCode.OVER_ATTENDEE_NUMBER);
        }
    }

    private void validateAuthorPermission(Long scheduleId, String userId){
        boolean isAuthor = travelAttendeeRepository
                .existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(scheduleId, userId, AttendeeRole.AUTHOR);

        if (!isAuthor){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_SHARE_ATTENDEE);
        }
    }

    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }


    private void validateNotAlreadyAttendee(Long scheduleId, Member guest){
        boolean isExistedAttendee = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, guest.getUserId());

        if (isExistedAttendee){
            throw new ConflictAttendeeException(ErrorCode.ALREADY_ATTENDEE);
        }
    }


    public void leaveScheduleAsGuest(Long scheduleId, String userId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (attendee.getRole().isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }

}
