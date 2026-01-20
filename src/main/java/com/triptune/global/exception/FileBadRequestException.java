package com.triptune.global.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class FileBadRequestException extends BusinessException{
    public FileBadRequestException(ErrorCode errorCode){
        super(errorCode);
    }
}
