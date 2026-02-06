package com.triptune.global.exception.handler;

import com.triptune.global.exception.*;
import com.triptune.global.response.ErrorResponse;
import com.triptune.global.security.jwt.exception.CustomJwtUnAuthorizedException;
import com.triptune.schedule.exception.chat.ChatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.frontend.404-error.url}")
    private String notFoundErrorURL;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request){
        log.warn("[{}] at {}: {}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(ex.getErrorCode()), ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request){
        log.warn("[MethodArgumentNotValidException] at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex.getBindingResult().getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining());

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(CustomJwtUnAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleCustomJwtUnAuthorizedException(CustomJwtUnAuthorizedException ex, HttpServletRequest request){
        log.warn("[CustomJwtUnAuthorizedException] at {}: {}", request.getRequestURI(),  ex.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(ex.getErrorCode()), ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle404(NoHandlerFoundException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("[NoHandlerFoundException] at {}: {}", request.getRequestURI(),  ex.getMessage());

        response.sendRedirect(notFoundErrorURL);
    }

    @MessageExceptionHandler(ChatException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleForbiddenChatException(ChatException ex){
        log.warn("[{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ErrorResponse.of(ex.getErrorCode());
    }

}
