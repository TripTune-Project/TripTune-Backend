package com.triptune.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class TokenDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{
        private String accessToken;
        private String refreshToken;

        @Builder
        public Request(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class RefreshResponse{
        private String accessToken;

        @Builder
        public RefreshResponse(String accessToken){
            this.accessToken = accessToken;
        }
    }

}
