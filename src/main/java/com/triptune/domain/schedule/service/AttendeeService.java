package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.request.CreateAttendeeRequest;
import com.triptune.domain.schedule.dto.response.AttendeeResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.AlreadyAttendeeException;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.service.TravelService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.triptune.domain.schedule.entity.QTravelAttendee.travelAttendee;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendeeService {

    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;


    public Page<AttendeeResponse> getAttendees(Long scheduleId, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelAttendee> travelAttendeePage = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelAttendeePage.map(AttendeeResponse::from);
    }


    public void createAttendee(Long scheduleId, String userId, CreateAttendeeRequest createAttendeeRequest) {
        TravelSchedule schedule = getSavedTravelSchedule(scheduleId);
        validateAuthorPermission(scheduleId, userId);

        Member guest = getSavedMember(createAttendeeRequest.getEmail());
        validateAttendeeNotExists(scheduleId, guest);

        TravelAttendee travelAttendee = TravelAttendee.of(schedule, guest, createAttendeeRequest.getPermission());
        travelAttendeeRepository.save(travelAttendee);
    }

    private void validateAuthorPermission(Long scheduleId, String userId){
        boolean isAuthor = travelAttendeeRepository
                .existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(scheduleId, userId, AttendeeRole.AUTHOR);

        if (!isAuthor){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_SHARE_ATTENDEE);
        }
    }

    private void validateAttendeeNotExists(Long scheduleId, Member guest){
        boolean isExistedAttendee = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, guest.getUserId());

        if (isExistedAttendee){
            throw new AlreadyAttendeeException(ErrorCode.ALREADY_ATTENDEE);
        }
    }


    public void removeAttendee(Long scheduleId, String userId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (attendee.isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }


    private Member getSavedMember(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }


    private TravelSchedule getSavedTravelSchedule(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }


}
