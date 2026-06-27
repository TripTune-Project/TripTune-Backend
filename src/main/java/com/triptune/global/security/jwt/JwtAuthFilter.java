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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.triptune.global.security.config.SecurityConstants.JWT_SKIP_LIST;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final JwtErrorResponseWriter jwtErrorResponseWriter;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/")){
            return true;
        }

        return Arrays.stream(JWT_SKIP_LIST)
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String token = jwtUtils.resolveToken(request);

            if (token != null){
                jwtUtils.validateToken(token);
                setAuthentication(token);
            }

            filterChain.doFilter(request, response);
        } catch (CustomJwtUnAuthorizedException ex){
            log.warn("[CustomJwtUnAuthorizedException] at {}: {}", request.getRequestURI(),  ex.getMessage());
            jwtErrorResponseWriter.write(response, ex.getErrorCode());
        }

    }

    private void setAuthentication(String token){
        Authentication auth = jwtUtils.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}
