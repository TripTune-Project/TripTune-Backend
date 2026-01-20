package com.triptune.global.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class DataNotFoundException extends BusinessException {
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
