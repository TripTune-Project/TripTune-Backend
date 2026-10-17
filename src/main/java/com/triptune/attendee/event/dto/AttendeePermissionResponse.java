package com.triptune.attendee.event.dto;

import com.triptune.attendee.enums.AttendeePermission;

public record AttendeePermissionResponse(
        Long attendeeId,
        AttendeePermission permission){
}
