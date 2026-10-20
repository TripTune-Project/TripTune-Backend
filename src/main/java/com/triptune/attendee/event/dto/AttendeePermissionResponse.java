package com.triptune.attendee.event.dto;

import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.attendee.enums.AttendeePermission;

public record AttendeePermissionResponse(
        Long attendeeId,
        AttendeePermission permission){

    public static AttendeePermissionResponse from(TravelAttendee attendee){
        return new AttendeePermissionResponse(
                attendee.getAttendeeId(),
                attendee.getPermission()
        );
    }
}
