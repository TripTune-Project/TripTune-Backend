package com.triptune.schedule.fixture;


import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.entity.TravelSchedule;
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


}
