package com.triptune.schedule.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;
import lombok.Getter;

@Getter
public class ForbiddenAttendeeException extends BusinessException {

    public ForbiddenAttendeeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
