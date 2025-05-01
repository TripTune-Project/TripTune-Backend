package com.triptune.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    // 인증이 완료되었으나 해당 엔드포인트에 접근할 권한이 없을 경우 발생

    private final HttpRequestEndpointChecker endpointChecker;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (!endpointChecker.isEndpointExist(request)){
            log.error("404 NotFoundException in CustomAccessDeniedHandler, Request URL: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "페이지를 찾을 수 없습니다.");
        } else{

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FORBIDDEN_ACCESS);
            String result = new ObjectMapper().writeValueAsString(errorResponse);

            response.getWriter().write(result);
            response.getWriter().flush();

            log.info("403 CustomAccessDeniedHandler(접근 권한 없음), URL: {}", request.getRequestURI());
        }

    }
}
