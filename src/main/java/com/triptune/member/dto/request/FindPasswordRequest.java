package com.triptune.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class FindPasswordRequest {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @Builder
    public FindPasswordRequest(String email) {
        this.email = email;
    }
}
