package com.triptune.domain.schedule.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat_message")
public class ChatMessage {
    @Id
    @Field("_id")
    private String messageId;
    private Long scheduleId;
    private Long memberId;
    private String nickname;
    private String message;
    private LocalDateTime timestamp;

    @Builder
    public ChatMessage(String messageId, Long scheduleId, Long memberId, String nickname, String message, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.scheduleId = scheduleId;
        this.memberId = memberId;
        this.nickname = nickname;
        this.message = message;
        this.timestamp = timestamp;
    }
}
