package com.triptune.attendee.dto.response;

import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.attendee.enums.AttendeePermission;

public record AttendeePermissionEventResponse(
        Long attendeeId,
        AttendeePermission permission){

    public static AttendeePermissionEventResponse from(TravelAttendee attendee){
        return new AttendeePermissionEventResponse(
                attendee.getAttendeeId(),
                attendee.getPermission()
        );
    }
}
