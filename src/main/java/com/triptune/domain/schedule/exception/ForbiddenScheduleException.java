package com.triptune.domain.schedule.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ForbiddenScheduleException extends RuntimeException{
    private final HttpStatus httpStatus;
    public ForbiddenScheduleException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
