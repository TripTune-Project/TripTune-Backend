package com.triptune.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    // 인증이 완료되었으나 해당 엔드포인트에 접근할 권한이 없을 경우 발생
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(HttpStatus.FORBIDDEN.value())
                .message("접근 권한이 없습니다.")
                .build();

        String result = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(result);
        response.getWriter().flush();
    }
}
