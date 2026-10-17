package com.triptune.chat.fixture;

import com.triptune.chat.dto.request.ChatMessageRequest;
import com.triptune.chat.entity.ChatMessage;

public class ChatMessageFixture {
    public static ChatMessage createChatMessage(Long scheduleId, Long memberId, String message){
        return ChatMessage.createChatMessage(
                scheduleId,
                memberId,
                message
        );
    }

    public static ChatMessageRequest createChatMessageRequest(Long scheduleId, String nickname, String message) {
        return ChatMessageRequest.builder()
                .scheduleId(scheduleId)
                .nickname(nickname)
                .message(message)
                .build();

    }
}
