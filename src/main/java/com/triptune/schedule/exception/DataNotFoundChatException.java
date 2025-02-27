package com.triptune.schedule.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataNotFoundChatException extends RuntimeException{
    private final HttpStatus httpStatus;

    public DataNotFoundChatException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
