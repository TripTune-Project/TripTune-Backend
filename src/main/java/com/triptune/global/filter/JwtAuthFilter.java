package com.triptune.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.exception.CustomJwtException;
import com.triptune.global.exception.ErrorCode;
import com.triptune.global.response.ErrorResponse;
import com.triptune.global.util.JwtUtil;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * JWT 가 유효성을 검증하는 Filter
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String LOGOUT_PATH = "/api/members/logout";
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // logout 요청일 경우 filter 통과
        if(requestURI.equals(LOGOUT_PATH)){
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.resolveToken(request);

        try{
            if (token != null && jwtUtil.validateToken(token)){
                Authentication auth = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (CustomJwtException ex){
            handleJwtException(response, ex);
        }

    }

    private void handleJwtException(HttpServletResponse response, CustomJwtException ex) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(ex.getHttpStatus().value());
        response.setCharacterEncoding(Charset.defaultCharset().name());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();

        String result = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(result);
        response.getWriter().flush();
    }
}
