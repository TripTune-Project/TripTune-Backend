package com.triptune.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String userId;

    @Builder
    public LoginResponse(String accessToken, String refreshToken, String userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }

    public static LoginResponse of(String accessToken, String refreshToken, String userId){
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .build();
    }


}