package com.triptune.global.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CookieType {
    ACCESS_TOKEN("accessToken", 3600, false),
    NICKNAME("nickname", 3600, false),
    REFRESH_TOKEN("refreshToken", 604800, true);

    private final String key;
    private final int maxAgeSeconds;
    private final boolean isHttpOnly;

}
