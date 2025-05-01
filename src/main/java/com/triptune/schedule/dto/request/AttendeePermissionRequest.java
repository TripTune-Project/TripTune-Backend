package com.triptune.schedule.dto.request;

import com.triptune.schedule.enums.AttendeePermission;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendeePermissionRequest {

    @NotNull(message = "허용 권한은 필수 입력 값입니다.")
    private AttendeePermission permission;

    @Builder
    public AttendeePermissionRequest(AttendeePermission permission){
        this.permission = permission;
    }

}
