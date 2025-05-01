package com.triptune.schedule.exception;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ForbiddenAttendeeException extends RuntimeException{
    private final HttpStatus httpStatus;

    public ForbiddenAttendeeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
