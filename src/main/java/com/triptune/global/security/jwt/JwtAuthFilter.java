package com.triptune.global.security.jwt;

import com.triptune.global.security.config.SecurityConstants;
import com.triptune.global.security.exception.CustomJwtUnAuthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(SecurityConstants.isWhitelisted(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtils.resolveToken(request);
        try{
            if (token != null && jwtUtils.validateToken(token)){
                Authentication auth = jwtUtils.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (CustomJwtUnAuthorizedException ex){
            log.error("CustomJwtUnAuthorizedException at {}: {}", request.getRequestURI(),  ex.getMessage());
            JwtUtils.writeJwtException(request, response, ex.getHttpStatus(), ex.getMessage());
        }

    }


}
