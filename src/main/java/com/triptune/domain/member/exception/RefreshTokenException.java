package com.triptune.domain.member.exception;

import com.triptune.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RefreshTokenException extends RuntimeException{
    private final HttpStatus httpStatus;

    public RefreshTokenException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
