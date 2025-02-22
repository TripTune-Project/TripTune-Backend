package com.triptune.global.config;

public class SecurityConstants {

    public static final String[] AUTH_WHITELIST = {
            "/swagger-ui/**", "/v3/api-docs/**", "/api/members/join", "/api/members/login",
            "/api/members/refresh", "/api/members/find-id", "/api/members/find-password", "/api/members/reset-password",
            "/api/emails/**", "/api/travels/**", "/h2-console/**", "/", "/error", "/ws"
    };

    private SecurityConstants(){
    }
}
