package com.triptune.domain.member;

import com.triptune.domain.BaseTest;
import com.triptune.domain.member.dto.request.ChangePasswordRequest;
import com.triptune.domain.member.dto.request.FindPasswordRequest;
import com.triptune.domain.member.dto.request.LogoutRequest;
import com.triptune.domain.member.dto.request.FindIdRequest;
import com.triptune.domain.member.dto.request.LoginRequest;
import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.member.dto.request.RefreshTokenRequest;

public class MemberTest extends BaseTest {

    protected MemberRequest createMemberRequest(){
        return MemberRequest.builder()
                .userId("testUser")
                .password("password123@")
                .rePassword("password123@")
                .nickname("test")
                .email("test@test.com")
                .build();
    }

    protected LoginRequest createLoginRequest(String userId, String password){
        return LoginRequest.builder()
                .userId(userId)
                .password(password)
                .build();
    }

    protected RefreshTokenRequest createRefreshTokenRequest(String refreshToken){
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    protected ChangePasswordRequest createChangePasswordDTO(String passwordToken, String newPassword1, String newPassword2){
        return ChangePasswordRequest.builder()
                .passwordToken(passwordToken)
                .password(newPassword1)
                .rePassword(newPassword2)
                .build();
    }

    protected LogoutRequest createLogoutDTO(String nickname){
        return LogoutRequest.builder()
                .nickname(nickname)
                .build();

    }


    protected FindIdRequest createFindIdRequest(String email){
        return FindIdRequest.builder()
                .email(email)
                .build();
    }

    protected FindPasswordRequest createFindPasswordDTO(String userId){
        return FindPasswordRequest.builder()
                .userId(userId)
                .email(userId + "@email.com")
                .build();
    }
}
