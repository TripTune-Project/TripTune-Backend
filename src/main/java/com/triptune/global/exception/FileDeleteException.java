package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class FileDeleteException extends BusinessException {
    public FileDeleteException(ErrorCode errorCode) {
        super(errorCode);
    }
}
