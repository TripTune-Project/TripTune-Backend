package com.triptune.global.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataExistException extends RuntimeException{
    private final HttpStatus httpStatus;

    public DataExistException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
