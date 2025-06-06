package com.triptune.member.exception.handler;

import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.exception.FailLoginException;
import com.triptune.global.response.ErrorResponse;
import com.triptune.member.exception.UnsupportedSocialMemberException;
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
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleChangeMemberInfoException(IncorrectPasswordException ex, HttpServletRequest request){
        log.error("ChangePasswordException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedSocialMember(UnsupportedSocialMemberException ex, HttpServletRequest request){
        log.error("UnsupportedSocialMemberException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

}
