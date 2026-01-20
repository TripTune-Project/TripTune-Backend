package com.triptune.schedule.exception.chat;

import com.triptune.global.response.enums.ErrorCode;

public class ForbiddenChatException extends ChatException {

    public ForbiddenChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
