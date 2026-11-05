package com.triptune.global.security.jwt;

import com.triptune.global.redis.RedisService;
import com.triptune.chat.exception.CustomJwtUnAuthorizedChatException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.redis.enums.RedisKeyType;
import com.triptune.global.security.jwt.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.security.CustomUserDetailsService;
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
public class JwtUtils {

    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_PREFIX = "Authorization";

    private final Key key;
    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @Value("${spring.jwt.token.access-token-ttl}")
    private long accessTokenTtl;

    @Value("${spring.jwt.token.refresh-token-ttl}")
    private long refreshTokenTtl;

    @Value("${spring.jwt.token.password-token-ttl}")
    private long passwordTokenTtl;

    @Value("${spring.jwt.token.logout-token-ttl}")
    private long logoutTokenTtl;

    public JwtUtils(@Value("${spring.jwt.secret}") String secretKey, CustomUserDetailsService userDetailsService, RedisService redisService){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
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


    public void validateToken(String token) {
        try {
            String memberId = getSubject(token);
            String blacklisted = redisService.getData(RedisKeyType.LOGOUT, memberId);

            if(token.equals(blacklisted)){
                throw new CustomJwtUnAuthorizedException(ErrorCode.BLACKLIST_TOKEN);
            }

        } catch (ExpiredJwtException e){
            throw new CustomJwtUnAuthorizedException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomJwtUnAuthorizedException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            throw new CustomJwtUnAuthorizedException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
            throw new CustomJwtUnAuthorizedException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    public void validateChatToken(String token) {
        try {
            String memberId = getSubject(token);
            String blacklisted = redisService.getData(RedisKeyType.LOGOUT, memberId);

            if(token.equals(blacklisted)){
                throw new CustomJwtUnAuthorizedChatException(ErrorCode.BLACKLIST_TOKEN);
            }

        } catch (ExpiredJwtException e){
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e){
            throw new CustomJwtUnAuthorizedChatException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e){
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

    public String getSubject(String token){
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();
   }

    public String createAccessToken(Long memberId){
        return createToken(memberId.toString(), accessTokenTtl);
    }

    public String createRefreshToken(Long memberId){
        return createToken(memberId.toString(), refreshTokenTtl);
    }

    public String createPasswordToken(String email){
        return createToken(email, passwordTokenTtl);
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

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtl / 1000;   // 밀리초 → 초
    }


    public long getLogoutTtlSeconds() {
        return logoutTokenTtl / 1000;
    }
}
