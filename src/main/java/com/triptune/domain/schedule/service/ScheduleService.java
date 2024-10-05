package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final AttendeeRepository attendeeRepository;

    public CreateScheduleResponse createSchedule(CreateScheduleRequest createScheduleRequest, String userId){
        TravelSchedule travelSchedule = TravelSchedule.builder()
                .scheduleName(createScheduleRequest.getScheduleName())
                .startDate(createScheduleRequest.getStartDate())
                .endDate(createScheduleRequest.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        TravelSchedule savedTravelSchedule = scheduleRepository.save(travelSchedule);

        Member savedMember = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));

        TravelAttendee travelAttendee = TravelAttendee.builder()
                .travelSchedule(savedTravelSchedule)
                .member(savedMember)
                .role(AttendeeRole.AUTHOR)
                .permission(AttendeePermission.ALL)
                .build();

        attendeeRepository.save(travelAttendee);

        return CreateScheduleResponse.entityToDto(savedTravelSchedule);
    }
}