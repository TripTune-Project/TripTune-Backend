package com.triptune.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeNicknameRequest {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z가-힣])[a-zA-Z가-힣\\d]{4,15}$",
            message = "닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다.")
    private String nickname;

    @Builder
    public ChangeNicknameRequest(String nickname){
        this.nickname = nickname;
    }

}
