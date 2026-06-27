package com.triptune.global.security.config;

public class SecurityConstants {

    public static final String[] AUTH_WHITELIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/h2-console/**",
            "/",
            "/error",
            "/error/**",
            "/ws",
            "/oauth2/authorization/**",
            "/login/oauth2/**"
    };

    public static final String[] JWT_SKIP_LIST = {
            "/api/members/join",
            "/api/members/login",
            "/api/members/refresh",
            "/api/members/find-id",
            "/api/members/find-password",
            "/api/members/reset-password",
            "/api/emails/**",
            "/api/travels/**"
    };

    private SecurityConstants() {}

}
