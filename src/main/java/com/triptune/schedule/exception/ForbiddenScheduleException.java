package com.triptune.schedule.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;

public class ForbiddenScheduleException extends BusinessException {
    public ForbiddenScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
