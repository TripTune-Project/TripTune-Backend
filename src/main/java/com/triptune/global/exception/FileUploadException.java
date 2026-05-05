package com.triptune.global.exception;

import com.triptune.global.message.ErrorCode;

public class FileUploadException extends BusinessException {
    public FileUploadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
