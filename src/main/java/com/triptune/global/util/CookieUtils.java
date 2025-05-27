package com.triptune.global.util;

import com.triptune.CookieType;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    public static String createCookie(CookieType cookieType, String value){
        return ResponseCookie.from(cookieType.getKey(), value)
                .httpOnly(true)
                .maxAge(cookieType.getMaxAgeSeconds())
                .secure(cookieType.isHttpOnly())
                .path("/")
                .sameSite("None")
                .build()
                .toString();
    }

    public static String deleteCookie(CookieType cookieType){
        return ResponseCookie.from(cookieType.getKey(), "")
                .httpOnly(true)
                .maxAge(0)
                .secure(cookieType.isHttpOnly())
                .path("/")
                .sameSite("None")
                .build()
                .toString();
    }
}
