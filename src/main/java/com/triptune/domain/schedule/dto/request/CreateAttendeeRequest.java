package com.triptune.domain.schedule.dto.request;

import com.triptune.domain.schedule.enumclass.AttendeePermission;
import jakarta.validation.constraints.Email;
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

    @Email
    @NotBlank(message = "공유할 사용자 이메일은 필수 입력 값입니다.")
    private String email;

    @NotNull(message = "허용 권한은 필수 입력 값입니다.")
    private AttendeePermission permission;

    @Builder
    public CreateAttendeeRequest(String email, AttendeePermission permission) {
        this.email = email;
        this.permission = permission;
    }
}
