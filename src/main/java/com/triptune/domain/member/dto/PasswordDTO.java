package com.triptune.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordDTO {
    private String passwordToken;
    private String password;
    private String repassword;

}
