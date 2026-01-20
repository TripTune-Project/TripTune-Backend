package com.triptune.member.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;


public class FailLoginException extends BusinessException {
    public FailLoginException(ErrorCode errorCode){
        super(errorCode);
    }
}
