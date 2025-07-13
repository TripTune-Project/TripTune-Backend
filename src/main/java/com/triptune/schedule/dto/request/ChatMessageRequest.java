package com.triptune.schedule.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {

    @NotNull(message = "일정 ID는 필수 입력 값입니다.")
    @Min(value = 1, message = "여행지 ID는 1 이상의 값이어야 합니다.")
    private Long scheduleId;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @NotBlank(message = "메시지는 필수 입력 값입니다.")
    @Max(value = 1000, message = "메시지는 1000자 까지만 입력 가능합니다.")
    private String message;

    @Builder
    public ChatMessageRequest(Long scheduleId, String nickname, String message) {
        this.scheduleId = scheduleId;
        this.nickname = nickname;
        this.message = message;
    }
}
