package com.triptune.schedule.entity;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_message")
public class ChatMessage {

    @Id
    @Field("_id")
    private String messageId;

    private Long scheduleId;
    private Long memberId;
    private String message;
    private Instant timestamp;

    private ChatMessage(Long scheduleId, Long memberId, String message, Instant timestamp) {
        this.scheduleId = scheduleId;
        this.memberId = memberId;
        this.message = message;
        this.timestamp = timestamp;
    }


    public static ChatMessage createChatMessage(Long scheduleId, Long memberId, String message) {
        return new ChatMessage(
                scheduleId,
                memberId,
                message,
                Instant.now()
        );
    }
}
