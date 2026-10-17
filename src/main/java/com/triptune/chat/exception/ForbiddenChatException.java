package com.triptune.chat.exception;

import com.triptune.global.message.ErrorCode;

public class ForbiddenChatException extends ChatException {

    public ForbiddenChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
