package com.triptune.domain.schedule.exception.handler;

import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
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
}
