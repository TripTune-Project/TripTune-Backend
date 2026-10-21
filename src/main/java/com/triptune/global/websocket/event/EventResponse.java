package com.triptune.global.websocket.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventResponse<T> {
    private EventType eventType;
    private String message;
    private T data;

    private EventResponse(EventType eventType, String message, T data) {
        this.eventType = eventType;
        this.message = message;
        this.data = data;
    }

    public static <T> EventResponse<T> of(EventType eventType, String message, T data){
        return new EventResponse<>(
                eventType,
                message,
                data
        );

    }


}
