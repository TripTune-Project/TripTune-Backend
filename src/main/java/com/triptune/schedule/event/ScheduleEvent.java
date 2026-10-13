package com.triptune.schedule.event;

public record ScheduleEvent<T>(
        Long scheduleId,
        ScheduleEventType eventType,
        String message,
        T data
) {
}
