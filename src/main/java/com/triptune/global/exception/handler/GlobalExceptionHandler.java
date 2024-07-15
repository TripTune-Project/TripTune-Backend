package com.triptune.global.exception.handler;

import com.triptune.global.exception.CustomJwtException;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request){
        log.info("MethodArgumentNotValidException : {}", ex.getMessage());

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining());

        return ErrorResponse.builder()
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .message(message).build();
    }


    @ExceptionHandler(CustomJwtException.class)
    public ErrorResponse handleCustomJwtException(CustomJwtException ex, HttpServletRequest request){
        log.info("CustomJwtException : {}", ex.getMessage());

        return ErrorResponse.builder()
                .errorCode(ex.getHttpStatus().value())
                .message(ex.getMessage()).build();
    }

}
