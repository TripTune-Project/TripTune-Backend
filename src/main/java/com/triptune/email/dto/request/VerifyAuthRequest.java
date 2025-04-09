package com.triptune.email.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyAuthRequest {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank(message = "인증번호는 필수 입력 값입니다.")
    private String authCode;

    @Builder
    public VerifyAuthRequest(String email, String authCode) {
        this.email = email;
        this.authCode = authCode;
    }
}
