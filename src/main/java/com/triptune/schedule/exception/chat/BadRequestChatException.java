package com.triptune.schedule.exception.chat;

import com.triptune.global.response.enums.ErrorCode;

public class BadRequestChatException extends ChatException {
    public BadRequestChatException(ErrorCode errorCode){
        super(errorCode);
    }
}
