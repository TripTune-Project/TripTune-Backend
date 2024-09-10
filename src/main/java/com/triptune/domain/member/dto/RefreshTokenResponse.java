package com.triptune.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenResponse {

    private String accessToken;

    @Builder
    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }


}
