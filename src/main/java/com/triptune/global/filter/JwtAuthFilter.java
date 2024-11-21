package com.triptune.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.response.ErrorResponse;
import com.triptune.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static com.triptune.global.config.SecurityConstants.AUTH_WHITELIST;

/**
 * JWT 가 유효성을 검증하는 Filter
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        return Arrays.stream(AUTH_WHITELIST).anyMatch(pattern -> {
            AntPathMatcher matcher = new AntPathMatcher();
            return matcher.match(pattern, requestURI);
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.resolveToken(request);
        try{
            if (token != null && jwtUtil.validateToken(token)){
                Authentication auth = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (CustomJwtBadRequestException ex){
            log.error("NoHandlerFoundException at {}: {}", request.getRequestURI(),  ex.getMessage());
            handleJwtException(response, ex.getHttpStatus(), ex.getMessage());
        } catch (CustomJwtUnAuthorizedException ex){
            log.error("CustomJwtUnAuthorizedException at {}: {}", request.getRequestURI(),  ex.getMessage());
            handleJwtException(response, ex.getHttpStatus(), ex.getMessage());
        }

    }

    private void handleJwtException(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(Charset.defaultCharset().name());

        ErrorResponse errorResponse = ErrorResponse.of(httpStatus, message);
        String result = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(result);
        response.getWriter().flush();
    }
}
