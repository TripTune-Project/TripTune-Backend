package com.triptune.domain.schedule.dto.response;

import com.triptune.domain.schedule.entity.TravelSchedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class OverviewScheduleResponse {
    private Long scheduleId;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String author;

    @Builder
    public OverviewScheduleResponse(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, String author) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.author = author;
    }

    public static OverviewScheduleResponse from(TravelSchedule travelSchedule, String author){
        return OverviewScheduleResponse.builder()
                .scheduleId(travelSchedule.getScheduleId())
                .scheduleName(travelSchedule.getScheduleName())
                .startDate(travelSchedule.getStartDate())
                .endDate(travelSchedule.getEndDate())
                .author(author).build();
    }
}
