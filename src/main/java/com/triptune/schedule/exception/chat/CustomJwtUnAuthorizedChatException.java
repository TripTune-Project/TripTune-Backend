package com.triptune.schedule.exception.chat;

import com.triptune.global.message.ErrorCode;

public class CustomJwtUnAuthorizedChatException extends ChatException {
    public CustomJwtUnAuthorizedChatException(ErrorCode errorCode){
        super(errorCode);
    }


}
