package com.triptune.global.exception.handler;

import com.triptune.global.exception.BusinessException;
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

    @Value("${app.frontend.404-error.url}")
    private String notFoundErrorURL;

    /**
     * BusinessException 을 상속 받은 예외들 처리
     * - ErrorCode 내부에 다양한 상태코드, 메시지 존재
     * - 응답 형태는 같지만 ErrorCode 만 다르게 처리하면 되서 ResponseEntity 반환하게 구성
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request){
        log.warn("[{}] at {}: {}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(ex.getErrorCode()), ex.getErrorCode().getStatus());
    }


    /**
     * JWT 인증 실패 처리
     * - ErrorCode 기반으로 동적 상태코드 반환
     * - 그래서 ResponseEntity 사용
     */
    @ExceptionHandler(CustomJwtUnAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleCustomJwtUnAuthorizedException(CustomJwtUnAuthorizedException ex, HttpServletRequest request){
        log.warn("[CustomJwtUnAuthorizedException] at {}: {}", request.getRequestURI(),  ex.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(ex.getErrorCode()), ex.getErrorCode().getStatus());
    }

    /**
     * @Valid 검증 실패 처리
     * - 상태 코드가 항상 400 BAD_REQUEST 로 고정
     * - 그래서 @ResponseStatus 사용해서 상태 지정 완료
     * - ResponseEntity 사용할 필요 없음
     */
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


    /**
     * 404 Error 처리
     * - 상태 코드 404로 고정
     * - 프론트 404 페이지로 이동시킴
     * - API 응답보다 사용자 페이지 이동 목적
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle404(NoHandlerFoundException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("[NoHandlerFoundException] at {}: {}", request.getRequestURI(),  ex.getMessage());

        response.sendRedirect(notFoundErrorURL);
    }

    /**
     * WebSocket 채팅 예외 처리
     * - Http 응답 구조 사용 안함
     * - 메시지 큐(/queue/errors) 로 에러 전달
     */
    @MessageExceptionHandler(ChatException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleForbiddenChatException(ChatException ex){
        log.warn("[{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ErrorResponse.of(ex.getErrorCode());
    }

}
