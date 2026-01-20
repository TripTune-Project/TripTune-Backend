package com.triptune.schedule.exception.chat;

import com.triptune.global.response.enums.ErrorCode;

public class CustomJwtUnAuthorizedChatException extends ChatException {
    public CustomJwtUnAuthorizedChatException(ErrorCode errorCode){
        super(errorCode);
    }


}
