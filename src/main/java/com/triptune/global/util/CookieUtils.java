package com.triptune.global.util;

import com.triptune.CookieType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    @Value("${app.domain}")
    private static String domainPath;

    public static String createCookie(CookieType cookieType, String value){
        return ResponseCookie.from(cookieType.getKey(), value)
//                .httpOnly(cookieType.isHttpOnly())
                .httpOnly(false)
                .maxAge(cookieType.getMaxAgeSeconds())
                .secure(true)
                .domain("." + domainPath)
                .path("/")
                .sameSite("None")
                .build()
                .toString();
    }

    public static String deleteCookie(CookieType cookieType){
        return ResponseCookie.from(cookieType.getKey(), "")
                .httpOnly(true)
                .maxAge(0)
                .secure(true)
                .path("/")
                .sameSite("None")
                .build()
                .toString();
    }
}
