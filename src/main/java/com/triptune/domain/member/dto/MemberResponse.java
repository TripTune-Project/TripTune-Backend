package com.triptune.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse {

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
    public MemberResponse(Long memberId, String userId, String password, String refreshToken, Boolean isSocialLogin, String nickname, String email, LocalDateTime createdAt, LocalDateTime updatedAt, Long fileId) {
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

    public static MemberResponse of(String userId){
        return MemberResponse.builder()
                .userId(userId)
                .build();
    }

}
