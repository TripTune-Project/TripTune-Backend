package com.triptune.member.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FailLoginException extends RuntimeException{
    private final HttpStatus httpStatus;

    public FailLoginException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
