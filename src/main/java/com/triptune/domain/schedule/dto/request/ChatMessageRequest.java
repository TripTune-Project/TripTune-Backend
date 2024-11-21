package com.triptune.domain.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {

    @NotNull(message = "일정 인덱스는 필수 입력 값입니다.")
    private Long scheduleId;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @NotBlank(message = "메시지는 필수 입력 값입니다.")
    private String message;

    @Builder
    public ChatMessageRequest(Long scheduleId, String nickname, String message) {
        this.scheduleId = scheduleId;
        this.nickname = nickname;
        this.message = message;
    }
}
