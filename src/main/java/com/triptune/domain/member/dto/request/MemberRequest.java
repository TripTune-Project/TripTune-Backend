package com.triptune.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[A-Za-z\\d]{4,15}$", message = "아이디 4자 이상 15자 이하의 대/소문자, 숫자만 사용 가능합니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 재입력은 필수 입력 값입니다.")
    private String rePassword;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[A-Za-z\\d]{4,15}$", message = "닉네임은 4자 이상 15자 이하의 영문 대/소문자, 한글, 숫자만 사용 가능합니다.")
    private String nickname;

    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Builder
    public MemberRequest(String userId, String password, String rePassword, String nickname, String email) {
        this.userId = userId;
        this.password = password;
        this.rePassword = rePassword;
        this.nickname = nickname;
        this.email = email;
    }
}
