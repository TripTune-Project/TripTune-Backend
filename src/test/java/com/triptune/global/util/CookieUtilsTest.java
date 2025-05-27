package com.triptune.global.util;

import com.triptune.CookieType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CookieUtilsTest {

    @Test
    @DisplayName("accessToken 쿠키 생성")
    void createAccessTokenCookie() {
        // given
        // when
        String response = CookieUtils.createCookie(CookieType.ACCESS_TOKEN, "accessTokenValue");

        // then
        assertThat(response).contains(CookieType.ACCESS_TOKEN.getKey() + "=accessTokenValue");
        assertThat(response).contains("Max-Age=" + CookieType.ACCESS_TOKEN.getMaxAgeSeconds());
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).doesNotContain("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("nickname 쿠키 생성")
    void createNicknameCookie() {
        // given
        // when
        String response = CookieUtils.createCookie(CookieType.NICKNAME, "nicknameValue");

        // then
        assertThat(response).contains(CookieType.NICKNAME.getKey() + "=nicknameValue");
        assertThat(response).contains("Max-Age=" + CookieType.NICKNAME.getMaxAgeSeconds());
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).doesNotContain("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("refreshToken 쿠키 생성")
    void createRefreshTokenCookie() {
        // given
        // when
        String response = CookieUtils.createCookie(CookieType.REFRESH_TOKEN, "refreshTokenValue");

        // then
        assertThat(response).contains(CookieType.REFRESH_TOKEN.getKey() + "=refreshTokenValue");
        assertThat(response).contains("Max-Age=" + CookieType.REFRESH_TOKEN.getMaxAgeSeconds());
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("accessToken 쿠키 삭제")
    void deleteAccessTokenCookie() {
        // given
        // when
        String response = CookieUtils.deleteCookie(CookieType.ACCESS_TOKEN);

        // then
        assertThat(response).contains(CookieType.ACCESS_TOKEN.getKey() + "=");
        assertThat(response).contains("Max-Age=" + 0);
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).doesNotContain("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("nickname 쿠키 삭제")
    void deleteNicknameCookie() {
        // given
        // when
        String response = CookieUtils.deleteCookie(CookieType.NICKNAME);

        // then
        assertThat(response).contains(CookieType.NICKNAME.getKey() + "=");
        assertThat(response).contains("Max-Age=" + 0);
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).doesNotContain("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("refreshToken 쿠키 삭제")
    void deleteRefreshTokenCookie() {
        // given
        // when
        String response = CookieUtils.deleteCookie(CookieType.REFRESH_TOKEN);

        // then
        assertThat(response).contains(CookieType.REFRESH_TOKEN.getKey() + "=");
        assertThat(response).contains("Max-Age=" + 0);
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


}