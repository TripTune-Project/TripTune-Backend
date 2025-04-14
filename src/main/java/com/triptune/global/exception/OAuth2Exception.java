package com.triptune.global.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OAuth2Exception extends RuntimeException{
    private final HttpStatus httpStatus;

    public OAuth2Exception(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
