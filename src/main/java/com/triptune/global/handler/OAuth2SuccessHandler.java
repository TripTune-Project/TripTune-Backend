package com.triptune.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.service.CustomUserDetails;
import com.triptune.global.util.JwtUtils;
import com.triptune.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Value("${app.frontend.main.url}")
    private String redirectURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.createAccessToken(userDetails.getUsername());

        Map<String, String> body = Map.of(
                "access_token", accessToken
        );

        Cookie refreshTokenCookie = createRefreshTokenCookie(userDetails);

        response.sendRedirect(redirectURL);
        response.addCookie(refreshTokenCookie);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private Cookie createRefreshTokenCookie(CustomUserDetails userDetails){
        String refreshToken = userDetails.getRefreshToken();

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        return cookie;
    }
}
