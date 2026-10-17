package com.triptune.schedule.event;

import com.triptune.attendee.enums.AttendeePermission;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ScheduleEventType {
    ATTENDEE_ADDED("%s님이 일정에 참여했습니다."),
    ATTENDEE_REMOVED("%s님이 일정에서 내보내졌습니다."),
    ATTENDEE_LEFT("%s님이 일정에서 나갔습니다."),
    ATTENDEE_PERMISSION_UPDATED("%s님의 접근 권한이 %s으로 변경되었습니다.");

    private final String message;


    public String format(String nickname) {
        return message.formatted(nickname);
    }

    public String format(String nickname, AttendeePermission permission) {
        return message.formatted(nickname, permission.getPermission());
    }

}
