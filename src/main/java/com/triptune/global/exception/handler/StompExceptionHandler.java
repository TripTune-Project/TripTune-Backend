package com.triptune.global.exception.handler;

import com.triptune.chat.exception.ChatException;
import com.triptune.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class StompExceptionHandler {

    /**
     * WebSocket 채팅 예외 처리
     * - 메시지 큐(/queue/errors) 로 에러 전달
     */
    @MessageExceptionHandler(ChatException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleForbiddenChatException(ChatException ex){
        log.warn("[{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ErrorResponse.of(ex.getErrorCode());
    }
}
