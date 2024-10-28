package com.triptune.domain.member;

import com.triptune.domain.BaseTest;
import com.triptune.domain.member.dto.ChangePasswordDTO;
import com.triptune.domain.member.dto.LogoutDTO;
import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.member.dto.request.RefreshTokenRequest;

public class MemberTest extends BaseTest {

    protected MemberRequest createMemberRequest(){
        return MemberRequest.builder()
                .userId("testUser")
                .password("password123@")
                .repassword("password123@")
                .nickname("test")
                .email("test@test.com")
                .build();
    }

    protected RefreshTokenRequest createRefreshTokenRequest(String refreshToken){
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    protected ChangePasswordDTO createChangePasswordDTO(String passwordToken, String newPassword){
        return ChangePasswordDTO.builder()
                .passwordToken(passwordToken)
                .password(newPassword)
                .repassword(newPassword)
                .build();
    }

    protected LogoutDTO createLogoutDTO(){
        return LogoutDTO.builder()
                .userId("test")
                .build();

    }
}
