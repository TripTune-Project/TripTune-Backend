package com.triptune.chat.exception;

import com.triptune.global.message.ErrorCode;

public class DataNotFoundChatException extends ChatException {

    public DataNotFoundChatException(ErrorCode errorCode){
        super(errorCode);
    }
}
