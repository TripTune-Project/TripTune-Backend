package com.triptune.domain.member.exception.handler;

import com.triptune.domain.member.exception.CustomUsernameNotFoundException;
import com.triptune.domain.common.exception.DataExistException;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFailLoginException(FailLoginException ex, HttpServletRequest request){
        log.error("FailLoginException at {}: {}", request.getRequestURI(), ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCustomUsernameNotFoundException(CustomUsernameNotFoundException ex, HttpServletRequest request){
        log.error("CustomUsernameNotFoundException at {}: {}", request.getRequestURI(), ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleChangePasswordException(ChangePasswordException ex, HttpServletRequest request){
        log.error("ChangePasswordException at {}: {}", request.getRequestURI(), ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
    }

}
