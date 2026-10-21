package com.triptune.global.websocket.event;

import com.triptune.attendee.enums.AttendeePermission;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EventType {
    // 일정
    SCHEDULE_UPDATED(null),

    // 참석자
    ATTENDEE_ADDED("%s님이 일정에 참여했습니다."),
    ATTENDEE_REMOVED("%s님이 일정에서 내보내졌습니다."),
    ATTENDEE_LEFT("%s님이 일정에서 나갔습니다."),
    ATTENDEE_PERMISSION_UPDATED("%s님의 접근 권한이 %s으로 변경되었습니다."),

    // 여행 루트
    ROUTE_ADDED(null),
    ROUTE_UPDATED(null),
    ROUTE_DELETED(null);

    private final String message;


    public String format(String nickname) {
        return message.formatted(nickname);
    }

    public String format(String nickname, String permission) {
        return message.formatted(nickname, permission);
    }

}
