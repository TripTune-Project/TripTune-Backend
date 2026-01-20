package com.triptune.global.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class CustomIllegalArgumentException extends BusinessException{
    public CustomIllegalArgumentException(ErrorCode errorCode) {
        super(errorCode);
    }
}

