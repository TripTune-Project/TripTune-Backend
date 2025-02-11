package com.triptune.global.exception.handler;

import com.triptune.global.exception.*;
import com.triptune.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.frontend.404-error.url}")
    private String notFoundErrorURL;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request){
        log.error("MethodArgumentNotValidException at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining());

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(CustomJwtUnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleCustomJwtUnAuthorizedException(CustomJwtUnAuthorizedException ex, HttpServletRequest request){
        log.error("CustomJwtUnAuthorizedException at {}: {}", request.getRequestURI(),  ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), request.getRequestURI() + " : " + ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle404(NoHandlerFoundException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("NoHandlerFoundException at {}: {}", request.getRequestURI(),  ex.getMessage());

        response.sendRedirect(notFoundErrorURL);
    }

    @ExceptionHandler(DataExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataExistedException(DataExistException ex, HttpServletRequest request){
        log.error("DataExistException at {}: {}", request.getRequestURI(),  ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataNotFoundException(DataNotFoundException ex, HttpServletRequest request){
        log.error("DataNotFoundException at {}: {}", request.getRequestURI(),  ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());

    }

    @ExceptionHandler(CustomNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomNotValidException(CustomNotValidException ex, HttpServletRequest request){
        log.error("CustomNotValidException at {}: {}", request.getRequestURI(),  ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(FileBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFileBadRequestException(FileBadRequestException ex, HttpServletRequest request){
        log.error("FileBadRequestException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(CustomIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomIllegalArgumentException(CustomIllegalArgumentException ex, HttpServletRequest request){
        log.error("CustomIllegalArgumentException at {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

}
