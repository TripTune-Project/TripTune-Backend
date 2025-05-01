package com.triptune.global.redis.eums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisKeyType {
    VERIFIED("isVerified", "이메일 인증 여부"),
    AUTH_CODE("authCode", "이메일 인증 번호");

    private final String keyType;
    private final String description;

}
