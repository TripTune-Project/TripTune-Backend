package com.triptune.domain.schedule.service;

import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
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

    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;

    public void removeAttendee(Long scheduleId, String userId) {
        TravelSchedule schedule = travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));

        TravelAttendee attendee = getAttendeeInfo(userId, schedule);

        if (isAuthor(attendee)){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);
        }

        travelAttendeeRepository.deleteById(attendee.getAttendeeId());
    }


    public TravelAttendee getAttendeeInfo(String userId, TravelSchedule schedule){
        return schedule.getTravelAttendeeList().stream()
                .filter(attendee -> attendee.getMember().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

    }

    public boolean isAuthor(TravelAttendee attendee){
        return attendee.getRole().equals(AttendeeRole.AUTHOR);
    }
}
