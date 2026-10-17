package com.triptune.chat.exception;

import com.triptune.global.message.ErrorCode;

public class BadRequestChatException extends ChatException {
    public BadRequestChatException(ErrorCode errorCode){
        super(errorCode);
    }
}
