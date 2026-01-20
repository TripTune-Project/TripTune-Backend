package com.triptune.schedule.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ForbiddenScheduleException extends BusinessException {
    public ForbiddenScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
