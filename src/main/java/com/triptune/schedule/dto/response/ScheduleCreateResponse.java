package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelSchedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleCreateResponse {

    private Long scheduleId;

    @Builder
    public ScheduleCreateResponse(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public static ScheduleCreateResponse from(TravelSchedule travelSchedule){
        return ScheduleCreateResponse.builder()
                .scheduleId(travelSchedule.getScheduleId())
                .build();
    }
}
