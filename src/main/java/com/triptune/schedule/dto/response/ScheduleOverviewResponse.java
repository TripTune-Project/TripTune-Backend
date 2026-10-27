package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelSchedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ScheduleOverviewResponse {
    private Long scheduleId;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String author;
    private Integer routeVersion;

    @Builder
    public ScheduleOverviewResponse(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, String author, Integer routeVersion) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.author = author;
        this.routeVersion = routeVersion;
    }

    public static ScheduleOverviewResponse from(TravelSchedule schedule, String author){
        return ScheduleOverviewResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .author(author)
                .routeVersion(schedule.getRouteVersion())
                .build();
    }
}
