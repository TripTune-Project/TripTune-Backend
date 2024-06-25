package com.triptune.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomExpiredJwtException extends RuntimeException{
    private final HttpStatus httpStatus;

    public CustomExpiredJwtException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }


}
