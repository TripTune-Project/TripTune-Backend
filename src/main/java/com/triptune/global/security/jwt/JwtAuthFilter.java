package com.triptune.global.security.jwt;

import com.triptune.global.security.jwt.exception.CustomJwtUnAuthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final JwtErrorResponseWriter jwtErrorResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/")){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            String token = jwtUtils.resolveToken(request);

            if (token != null){
                jwtUtils.validateToken(token);
                setAuthentication(token);
            }

            filterChain.doFilter(request, response);
        } catch (CustomJwtUnAuthorizedException ex){
            log.error("CustomJwtUnAuthorizedException at {}: {}", request.getRequestURI(),  ex.getMessage());
            jwtErrorResponseWriter.write(response, ex.getErrorCode());
        }

    }

    private void setAuthentication(String token){
        Authentication auth = jwtUtils.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}
