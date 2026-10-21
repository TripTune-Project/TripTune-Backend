package com.triptune.global.websocket.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleEvent<T> {
    private Long scheduleId;
    private EventType eventType;
    private String message;
    private T data;

    private ScheduleEvent(Long scheduleId, EventType eventType, String message, T data) {
        this.scheduleId = scheduleId;
        this.eventType = eventType;
        this.message = message;
        this.data = data;
    }

    public static <T> ScheduleEvent<T> of(Long scheduleId, EventType eventType, T data){
        return new ScheduleEvent<>(
                scheduleId,
                eventType,
                null,
                data
        );
    }

    public static <T> ScheduleEvent<T> withNickname(Long scheduleId, EventType eventType, String nickname, T data){
        return new ScheduleEvent<>(
                scheduleId,
                eventType,
                eventType.format(nickname),
                data
        );
    }


    public static <T> ScheduleEvent<T> withPermission(Long scheduleId, EventType eventType, String nickname, String permission, T data){
        return new ScheduleEvent<>(
                scheduleId,
                eventType,
                eventType.format(nickname, permission),
                data
        );
    }

}
