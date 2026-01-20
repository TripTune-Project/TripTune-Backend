package com.triptune.schedule.exception.chat;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;

@Getter
public abstract class ChatException extends RuntimeException{
    private final ErrorCode errorCode;

    protected ChatException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
