package com.triptune.route.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;

public class ConflictRouteVersionException extends BusinessException {
    public ConflictRouteVersionException(ErrorCode errorCode) {
        super(errorCode);
    }
}
