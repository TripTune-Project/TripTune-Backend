package com.triptune.schedule.fixture;


import com.triptune.member.entity.Member;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.travel.entity.TravelImage;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

public class TravelScheduleFixture {
    
    public static TravelSchedule createTravelSchedule(String scheduleName){
        return TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );
    }

    public static TravelSchedule createTravelScheduleWithId(Long scheduleId, String scheduleName){
        TravelSchedule travelSchedule = TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );

        ReflectionTestUtils.setField(travelSchedule, "scheduleId", scheduleId);

        return travelSchedule;
    }


    public static ScheduleCreateRequest createScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate) {
        return ScheduleCreateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public static ScheduleUpdateRequest createUpdateScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate, RouteRequest... routeRequest){
        return ScheduleUpdateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .travelRoutes(List.of(routeRequest))
                .build();
    }


    public static ScheduleInfoQueryDto createScheduleInfoQueryDto(TravelSchedule schedule, TravelAttendee current, TravelAttendee author, String thumbnailS3ObjectKey) {
        return ScheduleInfoQueryDto.builder()
                .scheduleId(schedule.getScheduleId())
                .attendeeRole(current.getRole())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .authorNickname(author.getMember().getNickname())
                .authorS3ObjectKey(author.getMember().getProfileImage().getS3ObjectKey())
                .build();
    }
}
