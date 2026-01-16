package com.triptune.global.util;

import com.triptune.global.security.CookieType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class CookieUtils {

    @Value("${app.domain}")
    private String domainPath;

    private static final String REFRESH_TOKEN_NAME = "refreshToken";

    public String createCookie(CookieType cookieType, String value){
        return ResponseCookie.from(cookieType.getKey(), value)
                .httpOnly(cookieType.isHttpOnly())
                .maxAge(cookieType.getMaxAgeSeconds())
                .secure(true)
                .domain("." + domainPath)
                .path("/")
                .sameSite("None")
                .build()
                .toString();
    }

    public String deleteCookie(CookieType cookieType){
        return ResponseCookie.from(cookieType.getKey(), "")
                .httpOnly(true)
                .maxAge(0)
                .domain("." + domainPath)
                .secure(true)
                .path("/")
                .sameSite("None")
                .build()
                .toString();
    }


    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request){
        if (request.getCookies() == null){
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_NAME))
                .map(Cookie::getValue)
                .findFirst();
    }


    public void deleteAllCookies(HttpServletResponse response){
        Stream.of(CookieType.ACCESS_TOKEN, CookieType.REFRESH_TOKEN, CookieType.NICKNAME)
                .forEach(type -> response.addHeader("Set-Cookie", deleteCookie(type)));
    }
}
