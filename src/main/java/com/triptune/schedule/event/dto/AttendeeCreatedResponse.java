package com.triptune.schedule.event.dto;

import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;

public record AttendeeCreatedResponse(
    Long attendeeId,
    String nickname,
    String email,
    String profileUrl,
    AttendeeRole role,
    AttendeePermission permission) {

    public static AttendeeCreatedResponse of(TravelAttendee attendee, String profileUrl){
        return new AttendeeCreatedResponse(
                attendee.getAttendeeId(),
                attendee.getMember().getNickname(),
                attendee.getMember().getEmail(),
                profileUrl,
                attendee.getRole(),
                attendee.getPermission());
    }
}

