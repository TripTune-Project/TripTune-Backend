package com.triptune.schedule.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleEventResponse<T> {
    private ScheduleEventType eventType;
    private String message;
    private T data;

    private ScheduleEventResponse(ScheduleEventType eventType, String message, T data) {
        this.eventType = eventType;
        this.message = message;
        this.data = data;
    }

    public static <T> ScheduleEventResponse<T> of(ScheduleEventType scheduleEventType, String message, T data){
        return new ScheduleEventResponse<>(
                scheduleEventType,
                message,
                data
        );

    }


}
