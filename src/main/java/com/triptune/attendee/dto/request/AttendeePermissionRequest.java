package com.triptune.attendee.dto.request;

import com.triptune.attendee.enums.AttendeePermission;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendeePermissionRequest {

    @NotNull(message = "참석자 권한은 필수 입력 값입니다.")
    private AttendeePermission permission;

    @Builder
    public AttendeePermissionRequest(AttendeePermission permission){
        this.permission = permission;
    }

}
