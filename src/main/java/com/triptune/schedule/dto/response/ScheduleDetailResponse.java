package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.global.response.page.PageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleDetailResponse {
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer scheduleVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PageResponse<PlaceResponse> placeList;

    @Builder
    public ScheduleDetailResponse(String scheduleName, LocalDate startDate, LocalDate endDate, Integer scheduleVersion, LocalDateTime createdAt, LocalDateTime updatedAt, PageResponse<PlaceResponse> placeList) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.scheduleVersion = scheduleVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.placeList = placeList;
    }

    public static ScheduleDetailResponse from(TravelSchedule schedule, PageResponse<PlaceResponse> placeResponse){
        return ScheduleDetailResponse.builder()
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .scheduleVersion(schedule.getScheduleVersion())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .placeList(placeResponse)
                .build();
    }
}
