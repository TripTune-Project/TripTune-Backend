package com.triptune.global.exception;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.HttpRequestEndpointChecker;
import com.triptune.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 인증이 안된 익명의 사용자가 인증이 필요한 엔드포인트로 접근한 경우 발생

    private final HttpRequestEndpointChecker endpointChecker;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (!endpointChecker.isEndpointExist(request)){
            log.error("404 NotFoundException in CustomAuthenticationEntryPoint, Request URL: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorCode.PAGE_NOT_FOUND.getMessage());
        } else{
            log.error("401 CustomAuthenticationEntryPoint(미인증 접근 시도), URL: {}", request.getRequestURI());
            log.error("Header 의 Authorization {}", request.getHeader("Authorization") != null ? "존재함" : "존재하지 않음");

            JwtUtil.writeJwtException(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
        }
    }
}
