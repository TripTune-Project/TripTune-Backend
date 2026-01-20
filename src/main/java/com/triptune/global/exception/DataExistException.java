package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class DataExistException extends BusinessException{
    public DataExistException(ErrorCode errorCode){
        super(errorCode);
    }
}
