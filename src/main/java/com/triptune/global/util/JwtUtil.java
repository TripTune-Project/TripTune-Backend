package com.triptune.global.util;

import com.triptune.global.exception.BadRequestException;
import com.triptune.global.exception.ErrorCode;
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

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    private final Key key;
    private final CustomUserDetailsService userDetailsService;

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey, CustomUserDetailsService userDetailsService){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
    }

    /**
     * 요청 헤더에서 Bearer 토큰 추출
     * @param request
     * @return Bearer 토큰 문자열, 토큰이 없거나 유효하지 않은 경우 null
     */
    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * JWT 토큰 검증
     * @param token
     * @return 토큰 유효한 경우 true, 아닌 경우 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token ", e);
            throw new BadRequestException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e){
            log.info("Expired JWT Token ", e);
            throw new JwtException(ErrorCode.EXPIRED_JWT_TOKEN.getMessage());
        } catch (UnsupportedJwtException e){
            log.info("Unsupported JWT Token ", e);
            throw new BadRequestException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            log.info("JWT claims string is empty ", e);
            throw new BadRequestException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    /**
     * 권한 정보 획득
     * @param token
     * @return UserDetails 를 이용해 얻은 권한 정보 Authentication
     */
    public Authentication getAuthentication(String token){
        Claims claims = parseClaims(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    /**
     * Jwt 토큰 복호화
      * @param token
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
     * Access 토큰 생성
     * @param userId
     * @return access token
     */
    public String createAccessToken(String userId){
        Claims claims = Jwts.claims().setSubject(userId);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userId){
        Claims claims = Jwts.claims().setSubject(userId);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
