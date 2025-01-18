package com.triptune.domain.mypage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPagePasswordRequest {

    @NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
    private String nowPassword;

    @NotBlank(message = "변경할 비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
    private String newPassword;

    @NotBlank(message = "비밀번호 재입력은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다.")
    private String rePassword;

    @Builder
    public MyPagePasswordRequest(String nowPassword, String newPassword, String rePassword) {
        this.nowPassword = nowPassword;
        this.newPassword = newPassword;
        this.rePassword = rePassword;
    }

    public boolean isMatchNewPassword() {
        return this.newPassword.equals(this.rePassword);
    }

    public boolean isMatchNowPassword(){
        return this.nowPassword.equals(this.newPassword);
    }
}
