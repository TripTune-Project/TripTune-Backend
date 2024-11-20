package com.triptune.global.util;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_PREFIX = "Authorization";

    private final Key key;
    private final CustomUserDetailsService userDetailsService;
    private final RedisUtil redisUtil;

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey, CustomUserDetailsService userDetailsService, RedisUtil redisUtil){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
    }

    /**
     * 요청 헤더에서 Bearer Token 추출
     * @param request: HttpServletRequest
     * @return Bearer 을 뺀 Token 문자열, Token 이 없거나 유효하지 않은 경우 null
     */
    public String resolveToken(HttpServletRequest request){
        return resolveBearerToken(request.getHeader(AUTHORIZATION_PREFIX));
    }

    /**
     * 문자열에서 Bearer Token 추출
     * @param bearerToken: Bearer 이 포함된 토큰
     * @return Bearer 을 뺀 Token 문자열, Token 이 없거나 유효하지 않은 경우 null
     */
    public String resolveBearerToken(String bearerToken){
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)){
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }

        return null;
    }

    /**
     * JWT 토큰 검증
     * @param token: 토큰 문자열
     * @return Token 이 유효한 경우 true, 아닌 경우 exception 발생
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            if(redisUtil.existData(token)){
                log.info("Already logged out user");
                throw new CustomJwtBadRequestException(ErrorCode.BLACKLIST_TOKEN);
            }

            return true;
        } catch (ExpiredJwtException e){
            log.info("Expired JWT Token ", e);
            throw new CustomJwtUnAuthorizedException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token ", e);
            throw new CustomJwtBadRequestException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            log.info("Unsupported JWT Token ", e);
            throw new CustomJwtBadRequestException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            log.info("JWT claims string is empty ", e);
            throw new CustomJwtBadRequestException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    /**
     * 권한 정보 획득
     * @param token: 토큰 문자열
     * @return UserDetails 를 이용해 얻은 권한 정보 Authentication
     */
    public Authentication getAuthentication(String token){
        Claims claims = parseClaims(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    /**
     * JWT 토큰 복호화
     * @param token: 토큰 문자열
     * @return token 을 이용해 복호화한 Claims
     */ 
   public Claims parseClaims(String token){
       return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();
   }


    /**
     * JWT 토큰 생성
     * @param userId: 사용자 아이디
     * @param expirationTime: 토큰 만료 시간
     * @return 생성된 토큰 문자열
     */
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

    /**
     * Token 의 유효기간 추출
     * @param token: 토큰 문자열
     * @return Token 유효기간
     */
    public Long getExpiration(String token){
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        long now = new Date().getTime();

        return (expiration.getTime() - now);
    }

}
