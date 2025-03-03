package com.triptune.schedule.exception.handler;

import com.triptune.schedule.exception.ConflictAttendeeException;
import com.triptune.schedule.exception.ForbiddenAttendeeException;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ScheduleExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenScheduleException(ForbiddenScheduleException ex, HttpServletRequest request){
        log.error("ForbiddenScheduleException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenAttendeeException(ForbiddenAttendeeException ex, HttpServletRequest request){
        log.error("ForbiddenAttendeeException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictAttendeeException(ConflictAttendeeException ex, HttpServletRequest request){
        log.error("ConflictAttendeeException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }


}
