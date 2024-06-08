package com.triptune.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LoginDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        private String userId;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        private String password;

        @Builder
        public Request(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Response{
        private String accessToken;
        private String refreshToken;

        @Builder
        public Response(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

}
