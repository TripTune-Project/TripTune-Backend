package com.triptune.domain.schedule.service;

import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendeeService {

    private final TravelAttendeeRepository travelAttendeeRepository;

    public void removeAttendee(Long scheduleId, String userId) {
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        if (attendee.isAuthor()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }


}
