package com.triptune.email.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class EmailVerifyException extends RuntimeException{
    private final HttpStatus httpStatus;

    public EmailVerifyException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
