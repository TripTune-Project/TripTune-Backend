package com.triptune.schedule.repository.dto;

import com.triptune.schedule.enums.AttendeeRole;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleInfoQueryDto {
    private Long scheduleId;
    private AttendeeRole attendeeRole;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String thumbnailS3ObjectKey;
    private String authorNickname;
    private String authorS3ObjectKey;

    @Builder
    public ScheduleInfoQueryDto(Long scheduleId, AttendeeRole attendeeRole, String scheduleName, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updatedAt, String thumbnailS3ObjectKey, String authorNickname, String authorS3ObjectKey) {
        this.scheduleId = scheduleId;
        this.attendeeRole = attendeeRole;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.thumbnailS3ObjectKey = thumbnailS3ObjectKey;
        this.authorNickname = authorNickname;
        this.authorS3ObjectKey = authorS3ObjectKey;
    }
}
