package com.triptune.global.security.oauth.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class OAuth2Exception extends BusinessException {
    public OAuth2Exception(ErrorCode errorCode) {
        super(errorCode);
    }
}
