package com.triptune.global.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getCurrentUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
