package com.triptune.global.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class DataExistException extends BusinessException{
    public DataExistException(ErrorCode errorCode){
        super(errorCode);
    }
}
