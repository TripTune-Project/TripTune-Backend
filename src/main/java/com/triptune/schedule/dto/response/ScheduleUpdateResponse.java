package com.triptune.schedule.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleUpdateResponse {
    private Integer scheduleVersion;

    @Builder
    public ScheduleUpdateResponse(Integer scheduleVersion) {
        this.scheduleVersion = scheduleVersion;
    }

    public static ScheduleUpdateResponse of(Integer scheduleVersion){
        return ScheduleUpdateResponse.builder()
                .scheduleVersion(scheduleVersion)
                .build();
    }
}
