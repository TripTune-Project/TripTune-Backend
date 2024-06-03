package com.triptune.member.exception;

import com.triptune.common.exception.ErrorCode;
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
