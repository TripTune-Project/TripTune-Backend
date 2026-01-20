package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class CustomNotValidException extends BusinessException{

    public CustomNotValidException(ErrorCode errorCode) {
        super(errorCode);
    }
}
