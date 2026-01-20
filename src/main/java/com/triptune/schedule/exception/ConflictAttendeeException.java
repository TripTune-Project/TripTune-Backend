package com.triptune.schedule.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;

public class ConflictAttendeeException extends BusinessException {
    public ConflictAttendeeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
