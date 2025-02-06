package com.triptune.domain.schedule.exception.handler;

import com.triptune.domain.schedule.exception.*;
import com.triptune.global.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ChatExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleForbiddenChatException(ForbiddenChatException ex){
        log.error("ForbiddenChatException: {}", ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleDataNotFoundChatException(DataNotFoundChatException ex){
        log.error("ChatNotFoundException: {}", ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleCustomJwtUnAuthorizedChatException(CustomJwtUnAuthorizedChatException ex){
        log.error("CustomJwtUnAuthorizedChatException: {}", ex.getMessage());
        return ErrorResponse.of(ex.getHttpStatus(), ex.getMessage());
    }

}
