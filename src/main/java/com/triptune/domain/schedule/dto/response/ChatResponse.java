package com.triptune.domain.schedule.dto.response;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.schedule.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatResponse {

    private String messageId;
    private String nickname;
    private String profileUrl;
    private String message;
    private LocalDateTime timestamp;


    @Builder
    public ChatResponse(String messageId, String nickname, String profileUrl, String message, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.message = message;
        this.timestamp = timestamp;
    }


    public static ChatResponse from(Member member, ChatMessage message){
        return ChatResponse.builder()
                .messageId(message.getMessageId())
                .nickname(member.getNickname())
                .profileUrl(member.getProfileImage().getS3ObjectUrl())
                .message(message.getMessage())
                .timestamp(message.getTimestamp())
                .build();
    }

}
