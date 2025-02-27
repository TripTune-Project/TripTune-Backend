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
    @Pattern(regexp = "^(?=.*[a-zA-Z])[A-Za-z\\d]{4,15}$", message = "닉네임은 4자 이상 15자 이하의 영문 대/소문자, 한글, 숫자만 사용 가능합니다.")
    private String nickname;

    @Builder
    public ChangeNicknameRequest(String nickname){
        this.nickname = nickname;
    }

}
