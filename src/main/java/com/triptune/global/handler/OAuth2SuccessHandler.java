package com.triptune.global.handler;

import com.triptune.global.service.CustomUserDetails;
import com.triptune.global.util.JwtUtils;
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

    @Value("${app.frontend.main.url}")
    private String redirectURL;

    @Value("${spring.jwt.token.access-expiration-time}")
    private int accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private int refreshExpirationTime;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공 후 SuccessHandler 진입");

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.createAccessToken(userDetails.getMemberId());
        Cookie accessTokenCookie = createCookie("accessToken", accessToken, false, accessExpirationTime);
        response.addCookie(accessTokenCookie);

        String encodeNickname = URLEncoder.encode(userDetails.getName(), StandardCharsets.UTF_8);
        Cookie nicknameCookie = createCookie("nickname", encodeNickname, false, accessExpirationTime);
        response.addCookie(nicknameCookie);

        Cookie refreshTokenCookie = createCookie("refreshToken", userDetails.getRefreshToken(), true, refreshExpirationTime);
        response.addCookie(refreshTokenCookie);

        log.info("[Set-Cookie] accessToken   | maxAge={}s | HttpOnly={}",
                accessExpirationTime / 1000, accessTokenCookie.isHttpOnly());
        log.info("[Set-Cookie] nickname      | maxAge={}s | HttpOnly={}",
                accessExpirationTime / 1000, nicknameCookie.isHttpOnly());
        log.info("[Set-Cookie] refreshToken | maxAge={}s | HttpOnly={}",
                refreshExpirationTime / 1000, refreshTokenCookie.isHttpOnly());

        log.info("Redirect To Frontend → {}", redirectURL);

        response.sendRedirect(redirectURL);
        log.info("소셜 로그인 성공");
    }

    private Cookie createCookie(String key, String value, boolean httpOnly, int maxAgeMillis){
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeMillis / 1000);

        return cookie;
    }
}
