package com.triptune.domain.common.exception.handler;

import com.triptune.domain.common.exception.DataExistException;
import com.triptune.domain.common.exception.DataNotFoundException;

import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataExistedException(DataExistException ex, HttpServletRequest request){
        log.error("DataExistException at {}: {}", request.getRequestURI(),  ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataNotFoundException(DataNotFoundException ex, HttpServletRequest request){
        log.error("DataNotFoundException at {}: {}", request.getRequestURI(),  ex.getMessage());

        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(ex.getHttpStatus().value())
                .build();

    }
}
