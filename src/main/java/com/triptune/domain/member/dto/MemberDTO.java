package com.triptune.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Builder;
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
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d]{4,15}$", message = "아이디 4자 이상 15자 이하의 대/소문자, 숫자만 사용 가능합니다.")
        private String userId;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
        private String password;

        @NotBlank(message = "비밀번호 재입력은 필수 입력 값입니다.")
        private String repassword;

        @NotBlank(message = "닉네임은 필수 입력 값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z가-힣])[A-Za-z가-힣\\d]{4,15}$", message = "닉네임은 4자 이상 15자 이하의 영문 대/소문자, 한글, 숫자만 사용 가능합니다.")
        private String nickname;

        @Email(message = "이메일 형식에 맞지 않습니다.")
        private String email;

    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response{
        private Long memberId;
        private String userId;
        private String password;
        private String refreshToken;
        private Boolean isSocialLogin;
        private String nickname;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long fileId;


        @Builder
        public Response(Long memberId, String userId, String password, String refreshToken, Boolean isSocialLogin, String nickname, String email, LocalDateTime createdAt, LocalDateTime updatedAt, Long fileId) {
            this.memberId = memberId;
            this.userId = userId;
            this.password = password;
            this.refreshToken = refreshToken;
            this.isSocialLogin = isSocialLogin;
            this.nickname = nickname;
            this.email = email;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.fileId = fileId;
        }

        public static Response of(String userId){
            return Response.builder()
                    .userId(userId)
                    .build();
        }
    }

}
