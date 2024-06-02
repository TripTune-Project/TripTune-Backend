package com.triptune.email.exception.handler;

import com.triptune.common.response.ErrorResponse;
import com.triptune.email.exception.EmailVerifyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmailExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmailVerifyExceptionHandler(EmailVerifyException ex){
        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();

    }
}
