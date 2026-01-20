package com.triptune.global.security.exception;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.security.jwt.JwtErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 인증이 안된 익명의 회원이 인증이 필요한 엔드포인트로 접근한 경우 발생

    private final JwtErrorResponseWriter jwtErrorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("401 CustomAuthenticationEntryPoint(미인증 접근 시도), URL: {}", request.getRequestURI());
        log.error("Header 의 Authorization {}", request.getHeader("Authorization") != null ? "존재함" : "존재하지 않음");

        jwtErrorResponseWriter.write(response, ErrorCode.UNAUTHORIZED_ACCESS);

    }
}
