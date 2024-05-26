package com.triptune.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


public class MemberDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{
        private String userId;
        private String password;
        private String refreshToken;
        private boolean isSocialLogin;
        private String nickname;
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
