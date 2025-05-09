package com.triptune.member.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IncorrectPasswordException extends RuntimeException{
    private final HttpStatus httpStatus;

    public IncorrectPasswordException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
