package com.triptune.domain.member.exception.handler;

import com.triptune.domain.member.exception.DataExistException;
import com.triptune.domain.member.exception.IncorrectPasswordException;
import com.triptune.domain.member.exception.RefreshTokenException;
import com.triptune.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataExistedException(DataExistException ex){
        log.error("DataExistException : {}", ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectPasswordException(IncorrectPasswordException ex){
        log.error("IncorrectPasswordException : {}", ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

    public ErrorResponse handleUsernameNotFoundException(UsernameNotFoundException ex){
        log.error("UsernameNotFoundException : {}", ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleRefreshTokenException(RefreshTokenException ex){
        log.error("RefreshTokenException : {}", ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

}
