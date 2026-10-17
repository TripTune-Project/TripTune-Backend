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

    @Builder
    public ScheduleOverviewResponse(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, String author) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.author = author;
    }

    public static ScheduleOverviewResponse from(TravelSchedule travelSchedule, String author){
        return ScheduleOverviewResponse.builder()
                .scheduleId(travelSchedule.getScheduleId())
                .scheduleName(travelSchedule.getScheduleName())
                .startDate(travelSchedule.getStartDate())
                .endDate(travelSchedule.getEndDate())
                .author(author).build();
    }
}
