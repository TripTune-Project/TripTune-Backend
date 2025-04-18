package com.triptune.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String nickname;

    @Builder
    public LoginResponse(String accessToken, String nickname) {
        this.accessToken = accessToken;
        this.nickname = nickname;
    }

    public static LoginResponse of(String accessToken, String nickname){
        return LoginResponse.builder()
                .accessToken(accessToken)
                .nickname(nickname)
                .build();
    }


}
