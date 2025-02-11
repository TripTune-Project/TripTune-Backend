package com.triptune.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.schedule.exception.CustomJwtUnAuthorizedChatException;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.response.ErrorResponse;
import com.triptune.global.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_PREFIX = "Authorization";

    private final Key key;
    private final CustomUserDetailsService userDetailsService;
    private final RedisUtils redisUtils;

    public JwtUtils(@Value("${spring.jwt.secret}") String secretKey, CustomUserDetailsService userDetailsService, RedisUtils redisUtils){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
        this.redisUtils = redisUtils;
    }


    public String resolveToken(HttpServletRequest request){
        return resolveBearerToken(request.getHeader(AUTHORIZATION_PREFIX));
    }


    public String resolveBearerToken(String bearerToken){
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)){
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }

        return null;
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            if(redisUtils.existData(token)){
                log.error("Already logged out user");
                throw new CustomJwtUnAuthorizedException(ErrorCode.BLACKLIST_TOKEN);
            }

            return true;
        } catch (ExpiredJwtException e){
            log.error("Expired JWT Token ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT Token ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            log.error("Unsupported JWT Token ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            log.error("JWT claims string is empty ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    public void validateChatToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            if(redisUtils.existData(token)){
                log.info("Already logged out user");
                throw new CustomJwtUnAuthorizedChatException(ErrorCode.BLACKLIST_TOKEN);
            }

        } catch (ExpiredJwtException e){
            log.info("Expired JWT Token ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            log.info("Unsupported JWT Token ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            log.info("JWT claims string is empty ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }



    public Authentication getAuthentication(String token){
        Claims claims = parseClaims(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


   public Claims parseClaims(String token){
       return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();
   }


    public String createToken(String userId, long expirationTime){
        Claims claims = Jwts.claims().setSubject(userId);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static void writeJwtException(HttpServletRequest request, HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(Charset.defaultCharset().name());

        ErrorResponse errorResponse = ErrorResponse.of(httpStatus, request.getRequestURI() + " : " + message);
        String result = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(result);
        response.getWriter().flush();
    }


}
