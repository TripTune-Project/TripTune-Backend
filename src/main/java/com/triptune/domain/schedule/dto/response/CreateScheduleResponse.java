package com.triptune.domain.schedule.dto.response;

import com.triptune.domain.schedule.entity.TravelSchedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateScheduleResponse {

    private Long scheduleId;

    @Builder
    public CreateScheduleResponse(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public static CreateScheduleResponse entityToDto(TravelSchedule travelSchedule){
        return CreateScheduleResponse.builder()
                .scheduleId(travelSchedule.getScheduleId())
                .build();
    }
}
