package com.triptune.chat.exception;

import com.triptune.global.message.ErrorCode;

public class CustomJwtUnAuthorizedChatException extends ChatException {
    public CustomJwtUnAuthorizedChatException(ErrorCode errorCode){
        super(errorCode);
    }


}
