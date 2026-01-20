package com.triptune.schedule.exception.chat;

import com.triptune.global.message.ErrorCode;

public class DataNotFoundChatException extends ChatException {

    public DataNotFoundChatException(ErrorCode errorCode){
        super(errorCode);
    }
}
