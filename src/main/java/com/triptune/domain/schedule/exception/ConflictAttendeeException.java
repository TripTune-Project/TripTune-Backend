package com.triptune.domain.schedule.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConflictAttendeeException extends RuntimeException{
    private final HttpStatus httpStatus;

    public ConflictAttendeeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
