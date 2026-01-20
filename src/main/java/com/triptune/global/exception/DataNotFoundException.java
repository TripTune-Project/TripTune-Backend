package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class DataNotFoundException extends BusinessException {
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
