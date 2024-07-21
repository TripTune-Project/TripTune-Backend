package com.triptune.domain.member.exception;

import com.triptune.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChangePasswordException extends RuntimeException{
    private final HttpStatus httpStatus;

    public ChangePasswordException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
