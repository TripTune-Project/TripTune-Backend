package com.triptune.global.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getCurrentEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
