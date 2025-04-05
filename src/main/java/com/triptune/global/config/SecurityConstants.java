package com.triptune.global.config;

import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

public class SecurityConstants {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public static final String[] AUTH_WHITELIST = {
            "/swagger-ui/**", "/v3/api-docs/**", "/api/members/join", "/api/members/login",
            "/api/members/refresh", "/api/members/find-id", "/api/members/find-password", "/api/members/reset-password",
            "/api/emails/**", "/api/travels/**", "/h2-console/**", "/", "/error", "/ws"
    };

    private SecurityConstants(){
    }

    public static boolean isWhitelisted(String requestURI){
        return Arrays.stream(AUTH_WHITELIST)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }
}
