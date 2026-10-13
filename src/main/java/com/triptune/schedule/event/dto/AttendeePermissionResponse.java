package com.triptune.schedule.event.dto;

import com.triptune.schedule.enums.AttendeePermission;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record AttendeePermissionResponse(
        Long attendeeId,
        AttendeePermission permission){
}
