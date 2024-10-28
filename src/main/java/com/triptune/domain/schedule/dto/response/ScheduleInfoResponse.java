package com.triptune.domain.schedule.dto.response;

import com.triptune.domain.schedule.dto.AuthorDTO;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.global.util.TimeDurationUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ScheduleInfoResponse {
    private Long scheduleId;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String sinceUpdate;
    private String thumbnailUrl;
    private AuthorDTO author;

    @Builder
    public ScheduleInfoResponse(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, String sinceUpdate, String thumbnailUrl, AuthorDTO author) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sinceUpdate = sinceUpdate;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
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

    public static ScheduleInfoResponse entityToDto(TravelSchedule schedule, String thumbnailUrl, AuthorDTO author){
        return ScheduleInfoResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .sinceUpdate(getSinceUpdate(schedule))
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .build();
    }

}
