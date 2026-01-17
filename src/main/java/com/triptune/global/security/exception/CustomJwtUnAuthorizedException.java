package com.triptune.global.security.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomJwtUnAuthorizedException extends RuntimeException{
    private final ErrorCode errorCode;

    public CustomJwtUnAuthorizedException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }


}
