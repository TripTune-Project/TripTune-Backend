package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class CustomIllegalArgumentException extends BusinessException{
    public CustomIllegalArgumentException(ErrorCode errorCode) {
        super(errorCode);
    }
}

