package com.triptune.chat.exception;

import com.triptune.global.message.ErrorCode;
import lombok.Getter;

@Getter
public abstract class ChatException extends RuntimeException{
    private final ErrorCode errorCode;

    protected ChatException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
