package com.triptune.global.util;

import com.triptune.global.security.CookieType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${app.domain}")
    private String domainPath;

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
}
