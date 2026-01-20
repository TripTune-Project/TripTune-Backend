package com.triptune.global.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class CustomNotValidException extends BusinessException{

    public CustomNotValidException(ErrorCode errorCode) {
        super(errorCode);
    }
}
