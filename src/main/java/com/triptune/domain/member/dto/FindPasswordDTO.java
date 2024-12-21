package com.triptune.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindPasswordDTO {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userId;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @Builder
    public FindPasswordDTO(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }
}
