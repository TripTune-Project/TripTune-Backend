package com.triptune.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequest {

    private String refreshToken;

    @Builder
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }


}
