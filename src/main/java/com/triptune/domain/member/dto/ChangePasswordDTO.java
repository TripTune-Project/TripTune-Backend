package com.triptune.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordDTO {
    private String passwordToken;
    private String password;
    private String repassword;

    @Builder
    public ChangePasswordDTO(String passwordToken, String password, String repassword) {
        this.passwordToken = passwordToken;
        this.password = password;
        this.repassword = repassword;
    }
}
