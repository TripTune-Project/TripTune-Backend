package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelSchedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleCreateResponse {

    private Long scheduleId;
    private Integer scheduleVersion;

    @Builder
    public ScheduleCreateResponse(Long scheduleId, Integer scheduleVersion) {
        this.scheduleId = scheduleId;
        this.scheduleVersion = scheduleVersion;
    }

    public static ScheduleCreateResponse from(TravelSchedule travelSchedule){
        return ScheduleCreateResponse.builder()
                .scheduleId(travelSchedule.getScheduleId())
                .scheduleVersion(travelSchedule.getScheduleVersion())
                .build();
    }
}
