package com.triptune.schedule.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestChatException extends RuntimeException{
    private final HttpStatus httpStatus;

    public BadRequestChatException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
