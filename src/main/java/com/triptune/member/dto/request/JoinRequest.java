package com.triptune.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class JoinRequest {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 재입력은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
    private String rePassword;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z가-힣])[a-zA-Z가-힣\\d]{4,15}$",
            message = "닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다.")
    private String nickname;


    @Builder
    public JoinRequest(String email, String password, String rePassword, String nickname) {
        this.email = email;
        this.password = password;
        this.rePassword = rePassword;
        this.nickname = nickname;
    }
}
