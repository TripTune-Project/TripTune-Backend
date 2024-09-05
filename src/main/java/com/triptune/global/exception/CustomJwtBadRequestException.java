package com.triptune.global.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomJwtBadRequestException extends RuntimeException{
    private final HttpStatus httpStatus;

    public CustomJwtBadRequestException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }


}
