package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
