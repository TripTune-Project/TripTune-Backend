package com.triptune.schedule.exception.chat;

import com.triptune.global.message.ErrorCode;

public class BadRequestChatException extends ChatException {
    public BadRequestChatException(ErrorCode errorCode){
        super(errorCode);
    }
}
