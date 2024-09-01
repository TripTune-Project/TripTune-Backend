package com.triptune.domain.common.exception.handler;

import com.triptune.domain.common.exception.DataNotFoundException;
import com.triptune.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataNotFoundException(DataNotFoundException ex){
        log.error("DataNotFoundException : {}", ex.getMessage());

        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(ex.getHttpStatus().value())
                .build();

    }
}
