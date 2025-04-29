package com.triptune.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.schedule.exception.CustomJwtUnAuthorizedChatException;
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

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Value("${spring.jwt.token.password-expiration-time}")
    private long passwordExpirationTime;

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
                log.error("이미 로그아웃 된 사용자의 토큰 유효성 검증 시도");
                throw new CustomJwtUnAuthorizedException(ErrorCode.BLACKLIST_TOKEN);
            }

            return true;
        } catch (ExpiredJwtException e){
            log.error("토큰 유효성 검증 중 만료된 JWT 토큰으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("토큰 유효성 검증 중 잘못된 JWT 토큰으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            log.error("토큰 유효성 검증 중 지원하지 않는 JWT 토큰으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            log.error("토큰 유효성 검증 중 비어있는 JWT 클레임으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    public void validateChatToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            if(redisUtils.existData(token)){
                log.error("채팅 중 이미 로그아웃 된 사용자의 토큰 유효성 검증 시도");
                throw new CustomJwtUnAuthorizedChatException(ErrorCode.BLACKLIST_TOKEN);
            }

        } catch (ExpiredJwtException e){
            log.error("채팅 중 만료된 JWT 토큰으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("채팅 중 잘못된 JWT 토큰으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            log.error("채팅 중 지원하지 않는 JWT 토큰으로 인증 시도 ", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            log.error("채팅 중 비어있는 JWT 클레임으로 인증 시도", e);
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }


    public Authentication getAuthentication(String token){
        Claims claims = parseClaims(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Long getMemberIdByToken(String token){
        String subject = parseClaims(token).getSubject();
        return subject != null ? Long.parseLong(subject) : null;
    }

    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();
   }

   public String createAccessToken(Long memberId){
        return createToken(memberId.toString(), accessExpirationTime);
   }

   public String createRefreshToken(Long memberId){
        return createToken(memberId.toString(), refreshExpirationTime);
   }

   public String createPasswordToken(String email){
        return createToken(email, passwordExpirationTime);
    }

   public String createToken(String subject, long expirationTime){
       Claims claims = Jwts.claims().setSubject(subject);
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
   }


}
