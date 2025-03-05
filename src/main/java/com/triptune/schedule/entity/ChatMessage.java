package com.triptune.schedule.entity;

import com.triptune.member.entity.Member;
import com.triptune.schedule.dto.request.ChatMessageRequest;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document(collection = "chat_message")
public class ChatMessage {

    @Id
    @Field("_id")
    private String messageId;

    private Long scheduleId;
    private Long memberId;
    private String message;
    private LocalDateTime timestamp;

    @Builder
    public ChatMessage(String messageId, Long scheduleId, Long memberId, String message, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.scheduleId = scheduleId;
        this.memberId = memberId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static ChatMessage of(Member member, ChatMessageRequest chatMessageRequest) {
        return ChatMessage.builder()
                .scheduleId(chatMessageRequest.getScheduleId())
                .memberId(member.getMemberId())
                .message(chatMessageRequest.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
