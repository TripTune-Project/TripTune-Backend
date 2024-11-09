package com.triptune.domain.schedule.dto.request;

import com.triptune.domain.schedule.enumclass.AttendeePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateAttendeeRequest {

    @NotBlank(message = "공유할 사용자 아이디는 필수 입력 값입니다.")
    private String userId;

    @NotNull(message = "허용 권한은 필수 입력 값입니다.")
    private AttendeePermission permission;

    @Builder
    public CreateAttendeeRequest(String userId, AttendeePermission permission) {
        this.userId = userId;
        this.permission = permission;
    }
}
