package com.triptune.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 인증이 안된 익명의 사용자가 인증이 필요한 엔드포인트로 접근한 경우 발생
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(Charset.defaultCharset().name());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(HttpStatus.UNAUTHORIZED.value())
                .message("인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요.")
                .build();

        String result = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(result);
        response.getWriter().flush();

    }
}
