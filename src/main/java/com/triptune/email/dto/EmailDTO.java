package com.triptune.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class EmailDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VerifyRequest{

        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        private String email;

        @Builder
        public VerifyRequest(String email) {
            this.email = email;
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class Verify{

        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        private String email;

        @NotBlank(message = "인증번호는 필수 입력 값입니다.")
        private String verifyCode;

        @Builder
        public Verify(String email, String verifyCode) {
            this.email = email;
            this.verifyCode = verifyCode;
        }
    }


}
