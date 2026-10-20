package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class CustomBadRequestException extends BusinessException{
    public CustomBadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}

