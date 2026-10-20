package com.triptune.schedule.event;

import com.triptune.attendee.enums.AttendeePermission;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleEvent<T> {
    private Long scheduleId;
    private ScheduleEventType eventType;
    private String message;
    private T data;

    private ScheduleEvent(Long scheduleId, ScheduleEventType eventType, String message, T data) {
        this.scheduleId = scheduleId;
        this.eventType = eventType;
        this.message = message;
        this.data = data;
    }

    public static <T> ScheduleEvent<T> of(Long scheduleId, ScheduleEventType eventType, T data){
        return new ScheduleEvent<>(
                scheduleId,
                eventType,
                null,
                data
        );
    }

    public static <T> ScheduleEvent<T> withNickname(Long scheduleId, ScheduleEventType eventType, String nickname, T data){
        return new ScheduleEvent<>(
                scheduleId,
                eventType,
                eventType.format(nickname),
                data
        );
    }


    public static <T> ScheduleEvent<T> withPermission(Long scheduleId, ScheduleEventType eventType, String nickname, AttendeePermission permission, T data){
        return new ScheduleEvent<>(
                scheduleId,
                eventType,
                eventType.format(nickname, permission),
                data
        );
    }

}
