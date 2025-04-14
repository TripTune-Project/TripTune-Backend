package com.triptune.member.enumclass;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomNotValidException;
import jakarta.security.auth.message.AuthException;

import javax.print.attribute.standard.MediaSize;

public enum SocialType {
    NAVER, KAKAO;

    public static SocialType of(String registrationId)  {
        return switch (registrationId){
            case "naver" -> NAVER;
            case "kakao" -> KAKAO;
            default -> throw new CustomNotValidException(ErrorCode.ILLEGAL_REGISTRATION_ID);
        };
    }

}
