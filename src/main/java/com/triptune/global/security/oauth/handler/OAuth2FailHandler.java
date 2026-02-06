package com.triptune.global.security.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.warn("[OAuth2 로그인 실패] at {}: {}", request.getRequestURI(), exception.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(Charset.defaultCharset().name());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI() + " : " + exception.getLocalizedMessage());
        String result = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(result);
        response.getWriter().flush();
    }
}
