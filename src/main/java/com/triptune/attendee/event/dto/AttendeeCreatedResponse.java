package com.triptune.attendee.event.dto;

import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.attendee.enums.AttendeePermission;
import com.triptune.attendee.enums.AttendeeRole;

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

