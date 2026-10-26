package com.triptune.schedule.dto.response;

import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.entity.TravelSchedule;
import java.time.LocalDate;

public record ScheduleUpdateEventResponse (
    String scheduleName,
    LocalDate startDate,
    LocalDate endDate,
    Integer scheduleVersion){

    public static ScheduleUpdateEventResponse from(TravelSchedule schedule){
        return new ScheduleUpdateEventResponse(
                schedule.getScheduleName(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getScheduleVersion()
        );
    }

    public static ScheduleUpdateEventResponse from(ScheduleUpdateRequest updateRequest, int newScheduleVersion) {
        return new ScheduleUpdateEventResponse(
                updateRequest.getScheduleName(),
                updateRequest.getStartDate(),
                updateRequest.getEndDate(),
                newScheduleVersion
        );
    }

}
