package com.triptune.attendee.dto.response;

import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.attendee.enums.AttendeePermission;
import com.triptune.attendee.enums.AttendeeRole;

public record AttendeeCreateEventResponse(
    Long attendeeId,
    String nickname,
    String email,
    String profileUrl,
    AttendeeRole role,
    AttendeePermission permission) {

    public static AttendeeCreateEventResponse of(TravelAttendee attendee, String profileUrl){
        return new AttendeeCreateEventResponse(
                attendee.getAttendeeId(),
                attendee.getMember().getNickname(),
                attendee.getMember().getEmail(),
                profileUrl,
                attendee.getRole(),
                attendee.getPermission());
    }
}

