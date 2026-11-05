package com.triptune.global.redis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisKeyType {
    // 이메일
    EMAIL_VERIFIED("email:isVerified:", "이메일 인증 여부, email:isVerified:이메일"),
    EMAIL_AUTH_CODE("email:authCode:", "이메일 인증 번호, email:authCode:이메일"),

    // 비밀번호
    RESET_PASSWORD("email:resetPassword:", "비밀번호 초기화 요청, email:resetPassword:이메일"),

    // 로그인
    LOGIN("login:refreshToken:", "로그인 한 사용자의 refreshToken 저장, login:refreshToken:memberId"),

    // 로그아웃
    LOGOUT("logout:blacklist:", "로그아웃 된 사용자 accessToken 저장, logout:blacklist:memberId");

    public static final String EMAIL_VERIFIED_VALUE = "true";

    private final String prefix;
    private final String description;

    public String generateKey(Object key){
        return prefix + key;
    }

}
