package com.triptune.schedule.dto.request;

import com.triptune.schedule.enums.AttendeePermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendeeRequest {

    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "공유할 회원의 이메일은 필수 입력 값입니다.")
    private String email;

    @NotNull(message = "참석자 권한은 필수 입력 값입니다.")
    private AttendeePermission permission;

    @Builder
    public AttendeeRequest(String email, AttendeePermission permission) {
        this.email = email;
        this.permission = permission;
    }
}
