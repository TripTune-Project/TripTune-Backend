package com.triptune.member.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


public class MemberDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{

        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d]{6,12}$", message = "아이디는 영문, 숫자 조합 6~12자리여야 합니다.")
        private String userId;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d]{8,15}$", message = "비밀번호는 영문, 숫자 조합 8~15자리여야 합니다.")
        private String password;

        @NotBlank(message = "비밀번호 재입력은 필수 입력 값입니다.")
        private String rePassword;

        private String refreshToken;

        private boolean isSocialLogin;

        @NotBlank(message = "닉네임은 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,8}$", message = "닉네임은 영문, 한글 포함 8자 이하여야 합니다.")
        private String nickname;

        @Email(message = "이메일 형식과 맞지 않습니다.")
        private String email;

        private Long fileId;
    }

    @Getter
    public static class Response{
        private Long memberId;
        private String userId;
        private String password;
        private String refreshToken;
        private boolean isSocialLogin;
        private String nickname;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long fileId;
    }





}
