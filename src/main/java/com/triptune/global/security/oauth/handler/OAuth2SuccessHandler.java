package com.triptune.global.security.oauth.handler;

import com.triptune.CookieType;
import com.triptune.global.security.CustomUserDetails;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;

    @Value("${app.frontend.main.url}")
    private String redirectURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공 후 SuccessHandler 진입");

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.createAccessToken(userDetails.getMemberId());
        response.addHeader("Set-Cookie", cookieUtils.createCookie(CookieType.ACCESS_TOKEN, accessToken));

        String encodeNickname = URLEncoder.encode(userDetails.getName(), StandardCharsets.UTF_8);
        response.addHeader("Set-Cookie", cookieUtils.createCookie(CookieType.NICKNAME, encodeNickname));

        response.addHeader("Set-Cookie", cookieUtils.createCookie(CookieType.REFRESH_TOKEN, userDetails.getRefreshToken()));

        log.info("[Set-Cookie] accessToken   | maxAge={}s | HttpOnly={}",
                CookieType.ACCESS_TOKEN.getMaxAgeSeconds(), CookieType.ACCESS_TOKEN.isHttpOnly());
        log.info("[Set-Cookie] nickname      | maxAge={}s | HttpOnly={}",
                CookieType.NICKNAME.getMaxAgeSeconds(), CookieType.NICKNAME.isHttpOnly());
        log.info("[Set-Cookie] refreshToken | maxAge={}s | HttpOnly={}",
                CookieType.REFRESH_TOKEN.getMaxAgeSeconds(), CookieType.REFRESH_TOKEN.isHttpOnly());

        log.info("Redirect To Frontend → {}", redirectURL);

        response.sendRedirect(redirectURL);
        log.info("소셜 로그인 성공");
    }

}
