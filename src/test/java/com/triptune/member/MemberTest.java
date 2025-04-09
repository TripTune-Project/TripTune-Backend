package com.triptune.member;

import com.triptune.BaseTest;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.member.dto.request.*;

public class MemberTest extends BaseTest {

    protected JoinRequest createMemberRequest(String email, String password, String repassword){
        return JoinRequest.builder()
                .email(email + "@email.com")
                .password(password)
                .rePassword(repassword)
                .nickname(email)
                .build();
    }

    protected LoginRequest createLoginRequest(String email, String password){
        return LoginRequest.builder()
                .email(email + "@email.com")
                .password(password)
                .build();
    }

    protected RefreshTokenRequest createRefreshTokenRequest(String refreshToken){
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    protected ResetPasswordRequest createResetPasswordRequest(String passwordToken, String newPassword1, String newPassword2){
        return ResetPasswordRequest.builder()
                .passwordToken(passwordToken)
                .password(newPassword1)
                .rePassword(newPassword2)
                .build();
    }

    protected LogoutRequest createLogoutRequest(String nickname){
        return LogoutRequest.builder()
                .nickname(nickname)
                .build();

    }

    protected FindPasswordRequest createFindPasswordRequest(String email){
        return FindPasswordRequest.builder()
                .email(email + "@email.com")
                .build();
    }

    protected ChangePasswordRequest createChangePasswordRequest(String nowPassword, String newPassword, String rePassword){
        return ChangePasswordRequest.builder()
                .nowPassword(nowPassword)
                .newPassword(newPassword)
                .rePassword(rePassword).build();
    }


    protected ChangeNicknameRequest createChangeNicknameRequest(String newNickname) {
        return ChangeNicknameRequest.builder()
                .nickname(newNickname)
                .build();
    }

    protected EmailRequest createEmailRequest(String email){
        return EmailRequest.builder()
                .email(email)
                .build();
    }

    protected DeactivateRequest createDeactivateRequest(String password){
        return DeactivateRequest.builder()
                .password(password)
                .build();
    }
}
