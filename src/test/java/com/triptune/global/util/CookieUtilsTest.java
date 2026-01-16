package com.triptune.global.util;

import com.triptune.global.security.CookieType;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("쿠키에서 refresh token 조회")
    void getRefreshTokenFromCookie() {
        // given
        String refreshTokenValue = "refreshTokenFromCookieTest";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieType.REFRESH_TOKEN.getKey(), refreshTokenValue));

        // when
        Optional<String> response = cookieUtils.getRefreshTokenFromCookie(request);

        // then
        assertThat(response.get()).isEqualTo(refreshTokenValue);

    }

    @Test
    @DisplayName("쿠키에서 refresh token 조회 결과 없어 null 반환")
    void getRefreshTokenFromCookie_returnNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        Optional<String> response = cookieUtils.getRefreshTokenFromCookie(request);

        // then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("전체 쿠키 삭제")
    void deleteAllCookies() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        cookieUtils.deleteAllCookies(response);

        // then
        List<String> cookies = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey())
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey())
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey())
                        && c.contains("Max-Age=0")
        );
    }


}