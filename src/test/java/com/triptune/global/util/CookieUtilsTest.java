package com.triptune.global.util;

import com.triptune.global.security.CookieType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {

    @InjectMocks private CookieUtils cookieUtils;

    @Test
    @DisplayName("accessToken 쿠키 생성")
    void createAccessTokenCookie() {
        // given
        // when
        String response = cookieUtils.createCookie(CookieType.ACCESS_TOKEN, "accessTokenValue");

        // then
        assertThat(response).contains(CookieType.ACCESS_TOKEN.getKey() + "=accessTokenValue");
        assertThat(response).contains("Max-Age=" + CookieType.ACCESS_TOKEN.getMaxAgeSeconds());
        assertThat(response).contains("Path=/");
        assertThat(response).doesNotContain("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("nickname 쿠키 생성")
    void createNicknameCookie() {
        // given
        // when
        String response = cookieUtils.createCookie(CookieType.NICKNAME, "nicknameValue");

        // then
        assertThat(response).contains(CookieType.NICKNAME.getKey() + "=nicknameValue");
        assertThat(response).contains("Max-Age=" + CookieType.NICKNAME.getMaxAgeSeconds());
        assertThat(response).contains("Path=/");
        assertThat(response).doesNotContain("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("refreshToken 쿠키 생성")
    void createRefreshTokenCookie() {
        // given
        // when
        String response = cookieUtils.createCookie(CookieType.REFRESH_TOKEN, "refreshTokenValue");

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
        String response = cookieUtils.deleteCookie(CookieType.ACCESS_TOKEN);

        // then
        assertThat(response).contains(CookieType.ACCESS_TOKEN.getKey() + "=");
        assertThat(response).contains("Max-Age=" + 0);
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("nickname 쿠키 삭제")
    void deleteNicknameCookie() {
        // given
        // when
        String response = cookieUtils.deleteCookie(CookieType.NICKNAME);

        // then
        assertThat(response).contains(CookieType.NICKNAME.getKey() + "=");
        assertThat(response).contains("Max-Age=" + 0);
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


    @Test
    @DisplayName("refreshToken 쿠키 삭제")
    void deleteRefreshTokenCookie() {
        // given
        // when
        String response = cookieUtils.deleteCookie(CookieType.REFRESH_TOKEN);

        // then
        assertThat(response).contains(CookieType.REFRESH_TOKEN.getKey() + "=");
        assertThat(response).contains("Max-Age=" + 0);
        assertThat(response).contains("Path=/");
        assertThat(response).contains("HttpOnly");
        assertThat(response).contains("Secure");
        assertThat(response).contains("SameSite=None");
    }


}