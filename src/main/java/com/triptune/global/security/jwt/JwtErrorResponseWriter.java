package com.triptune.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.response.ErrorResponse;
import com.triptune.global.response.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;

@Component
@RequiredArgsConstructor
public class JwtErrorResponseWriter {
    private final ObjectMapper objectMapper;

    public void writeJwtException(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(Charset.defaultCharset().name());

        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus(), errorCode.getMessage());
        String result = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(result);
    }


}
