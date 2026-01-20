package com.triptune.global.security.exception;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.security.jwt.JwtErrorResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    // 인증이 완료되었으나 해당 엔드포인트에 접근할 권한이 없을 경우 발생

    private final JwtErrorResponseWriter jwtErrorResponseWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("403 CustomAccessDeniedHandler(접근 권한 없음), URL: {}", request.getRequestURI());
        jwtErrorResponseWriter.write(response, ErrorCode.FORBIDDEN_ACCESS);

    }
}
