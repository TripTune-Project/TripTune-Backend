package com.triptune.domain.schedule.dto;

import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.travel.dto.TravelSimpleResponse;
import com.triptune.global.response.PageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor
public class ScheduleResponse {
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private List<AttendeeDTO> attendeeList;
    private PageResponse<TravelSimpleResponse> placeList;

    @Builder
    public ScheduleResponse(String scheduleName, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updateAt, List<AttendeeDTO> attendeeList, PageResponse<TravelSimpleResponse> placeList) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.attendeeList = attendeeList;
        this.placeList = placeList;
    }

    public static ScheduleResponse entityToDTO(TravelSchedule schedule, PageResponse<TravelSimpleResponse> simplePlaces, List<AttendeeDTO> attendeeList){
        return ScheduleResponse.builder()
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .createdAt(schedule.getCreatedAt())
                .updateAt(schedule.getUpdatedAt())
                .attendeeList(attendeeList)
                .placeList(simplePlaces)
                .build();
    }
}
