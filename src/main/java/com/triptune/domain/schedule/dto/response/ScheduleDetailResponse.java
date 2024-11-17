package com.triptune.domain.schedule.dto.response;

import com.triptune.domain.schedule.dto.AttendeeDTO;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.global.response.pagination.PageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ScheduleDetailResponse {
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private PageResponse<PlaceResponse> placeList;

    @Builder
    public ScheduleDetailResponse(String scheduleName, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updateAt, PageResponse<PlaceResponse> placeList) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.placeList = placeList;
    }

    public static ScheduleDetailResponse from(TravelSchedule schedule, PageResponse<PlaceResponse> placeResponse){
        return ScheduleDetailResponse.builder()
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .createdAt(schedule.getCreatedAt())
                .updateAt(schedule.getUpdatedAt())
                .placeList(placeResponse)
                .build();
    }
}
