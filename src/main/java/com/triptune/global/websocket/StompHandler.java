package com.triptune.global.websocket;

import com.triptune.global.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("WebSocket 연결 요청: {}", accessor.getSessionId());

            String token = jwtUtils.resolveBearerToken(accessor.getFirstNativeHeader("Authorization"));
            jwtUtils.validateChatToken(token);

            log.info("WebSocket 연결 완료: {}", accessor.getSessionId());
        }

        return message;
    }
}
