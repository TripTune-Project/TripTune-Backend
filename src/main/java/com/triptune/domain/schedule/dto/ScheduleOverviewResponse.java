package com.triptune.domain.schedule.dto;

import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.global.util.TimeDurationUtil;
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
    private String sinceUpdate;
    private String thumbnailUrl;

    @Builder
    public ScheduleOverviewResponse(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, String sinceUpdate, String thumbnailUrl) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sinceUpdate = sinceUpdate;
        this.thumbnailUrl = thumbnailUrl;
    }

    private static String getSinceUpdate(TravelSchedule schedule){
        String sinceUp = "";

        if (schedule.getUpdatedAt() != null){
            sinceUp = TimeDurationUtil.timeDuration(schedule.getUpdatedAt());
        } else if(schedule.getCreatedAt() != null){
            sinceUp = TimeDurationUtil.timeDuration(schedule.getCreatedAt());
        }

        return sinceUp;
    }

    public static ScheduleOverviewResponse entityToDto(TravelSchedule schedule, String thumbnailUrl){
        return ScheduleOverviewResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .sinceUpdate(getSinceUpdate(schedule))
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

}
