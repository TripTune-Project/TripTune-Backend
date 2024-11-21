package com.triptune.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogoutDTO {
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @Builder
    public LogoutDTO(String nickname) {
        this.nickname = nickname;
    }
}
