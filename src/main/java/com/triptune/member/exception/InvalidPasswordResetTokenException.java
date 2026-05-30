package com.triptune.member.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.message.ErrorCode;

public class InvalidPasswordResetTokenException extends BusinessException {
    public InvalidPasswordResetTokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
