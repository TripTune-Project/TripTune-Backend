package com.triptune.schedule.dto.response;

import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.Member;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.global.util.TimeUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
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


    public static ChatResponse from(ChatMessage message, MemberProfileResponse memberProfileResponse){
        return ChatResponse.builder()
                .messageId(message.getMessageId())
                .nickname(memberProfileResponse.getNickname())
                .profileUrl(memberProfileResponse.getProfileUrl())
                .message(message.getMessage())
                .timestamp(TimeUtils.convertToKST(message.getTimestamp()))
                .build();
    }

    public static ChatResponse from(ChatMessage message, Member member){
        return ChatResponse.builder()
                .messageId(message.getMessageId())
                .nickname(member.getNickname())
                .profileUrl(member.getProfileImage().getS3ObjectUrl())
                .message(message.getMessage())
                .timestamp(TimeUtils.convertToKST(message.getTimestamp()))
                .build();
    }
}
