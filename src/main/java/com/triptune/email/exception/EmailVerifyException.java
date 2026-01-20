package com.triptune.email.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;

public class EmailVerifyException extends BusinessException {
    public EmailVerifyException(ErrorCode errorCode){
        super(errorCode);
    }
}
