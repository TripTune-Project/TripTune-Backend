package com.triptune.global.security.config;

import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

public class SecurityConstants {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public static final String[] AUTH_WHITELIST = {
            "/swagger-ui/**", "/v3/api-docs/**", "/api/members/join", "/api/members/login",
            "/api/members/refresh", "/api/members/find-id", "/api/members/find-password",
            "/api/members/reset-password", "/api/emails/**", "/api/travels/popular",
            "/api/travels/recommend", "/h2-console/**", "/", "/error", "/ws",
            "/oauth2/authorization/**", "/login/oauth2/**"
    };

    public static final String[] AUTH_TRAVEL_LIST = {
            "/api/travels/**"
    };

    private SecurityConstants(){
    }

    public static boolean isWhitelisted(String requestURI){
        return Arrays.stream(AUTH_WHITELIST)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    public static boolean isTargetedTravelEndpoint(String requestURI){
        return Arrays.stream(AUTH_TRAVEL_LIST)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }
}
