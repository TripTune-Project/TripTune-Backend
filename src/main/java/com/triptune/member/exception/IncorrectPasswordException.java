package com.triptune.member.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;


public class IncorrectPasswordException extends BusinessException {
    public IncorrectPasswordException(ErrorCode errorCode){
        super(errorCode);
    }
}
