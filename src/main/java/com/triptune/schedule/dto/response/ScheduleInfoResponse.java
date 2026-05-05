package com.triptune.schedule.dto.response;

import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.schedule.service.dto.AuthorDTO;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.global.util.TimeUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
public class ScheduleInfoResponse {
    private Long scheduleId;
    private AttendeeRole role;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String sinceUpdate;
    private String thumbnailUrl;
    private AuthorDTO author;

    @Builder
    public ScheduleInfoResponse(Long scheduleId, AttendeeRole role, String scheduleName, LocalDate startDate, LocalDate endDate, String sinceUpdate, String thumbnailUrl, AuthorDTO author) {
        this.scheduleId = scheduleId;
        this.role = role;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sinceUpdate = sinceUpdate;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
    }

    public static ScheduleInfoResponse of(ScheduleInfoQueryDto scheduleInfo, String thumbnailUrl, AuthorDTO author){
        return ScheduleInfoResponse.builder()
                .scheduleId(scheduleInfo.getScheduleId())
                .role(scheduleInfo.getAttendeeRole())
                .scheduleName(scheduleInfo.getScheduleName())
                .startDate(scheduleInfo.getStartDate())
                .endDate(scheduleInfo.getEndDate())
                .sinceUpdate(getSinceUpdate(scheduleInfo))
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .build();
    }

    private static String getSinceUpdate(ScheduleInfoQueryDto scheduleInfo){
        String sinceUp = "";

        if (scheduleInfo.getUpdatedAt() != null){
            sinceUp = TimeUtils.timeDuration(scheduleInfo.getUpdatedAt());
        } else if(scheduleInfo.getCreatedAt() != null){
            sinceUp = TimeUtils.timeDuration(scheduleInfo.getCreatedAt());
        }

        return sinceUp;
    }

}
