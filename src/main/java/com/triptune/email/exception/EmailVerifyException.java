package com.triptune.email.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class EmailVerifyException extends BusinessException {
    public EmailVerifyException(ErrorCode errorCode){
        super(errorCode);
    }
}
