package com.triptune.member.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;


public class FailLoginException extends BusinessException {
    public FailLoginException(ErrorCode errorCode){
        super(errorCode);
    }
}
