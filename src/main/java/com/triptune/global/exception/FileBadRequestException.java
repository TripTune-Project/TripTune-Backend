package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class FileBadRequestException extends BusinessException{
    public FileBadRequestException(ErrorCode errorCode){
        super(errorCode);
    }
}
