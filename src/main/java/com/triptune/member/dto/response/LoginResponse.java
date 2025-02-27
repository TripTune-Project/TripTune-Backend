package com.triptune.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String nickname;

    @Builder
    public LoginResponse(String accessToken, String refreshToken, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.nickname = nickname;
    }

    public static LoginResponse of(String accessToken, String refreshToken, String nickname){
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nickname(nickname)
                .build();
    }


}
