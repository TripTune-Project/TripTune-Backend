package com.triptune.member.fixture;

import com.triptune.email.dto.request.EmailRequest;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import jakarta.servlet.http.Cookie;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

    public static Member createNativeTypeMember(String email, String encodePassword, ProfileImage profileImage){
        return Member.createNativeMember(
                email,
                encodePassword,
                email.split("@")[0],
                profileImage
        );
    }

    public static Member createNativeTypeMember(String email, ProfileImage profileImage){
        return Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );
    }

    public static Member createNativeTypeMemberWithId(Long memberId, String email, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }


    public static Member createSocialTypeMember(String email, ProfileImage profileImage){
        return Member.createSocialMember(
                email,
                email.split("@")[0],
                profileImage
        );
    }

    public static Member createSocialTypeMemberWithId(Long memberId, String email, ProfileImage profileImage){
        Member member = Member.createSocialMember(
                email,
                email.split("@")[0],
                profileImage
        );

        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;

    }

    public static Member createBothTypeMember(String email, String encodedPassword, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                encodedPassword,
                email.split("@")[0],
                profileImage
        );
        member.linkSocialAccount();

        return member;
    }

    public static Member createBothTypeMember(String email, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );
        member.linkSocialAccount();

        return member;
    }

    public static Member createBothTypeMemberWithId(Long memberId, String email, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );

        ReflectionTestUtils.setField(member, "memberId", memberId);
        member.linkSocialAccount();

        return member;
    }

    public static JoinRequest createMemberRequest(String email, String password, String repassword, String nickname){
        return JoinRequest.builder()
                .email(email)
                .password(password)
                .rePassword(repassword)
                .nickname(nickname)
                .build();
    }

    public static LoginRequest createLoginRequest(String email, String password){
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
    }


    public static ResetPasswordRequest createResetPasswordRequest(String passwordToken, String password, String rePassword){
        return ResetPasswordRequest.builder()
                .passwordToken(passwordToken)
                .password(password)
                .rePassword(rePassword)
                .build();
    }

    public static LogoutRequest createLogoutRequest(String nickname){
        return LogoutRequest.builder()
                .nickname(nickname)
                .build();

    }

    public static FindPasswordRequest createFindPasswordRequest(String email){
        return FindPasswordRequest.builder()
                .email(email)
                .build();
    }

    public static ChangePasswordRequest createChangePasswordRequest(String nowPassword, String newPassword, String rePassword){
        return ChangePasswordRequest.builder()
                .nowPassword(nowPassword)
                .newPassword(newPassword)
                .rePassword(rePassword).build();
    }


    public static ChangeNicknameRequest createChangeNicknameRequest(String newNickname) {
        return ChangeNicknameRequest.builder()
                .nickname(newNickname)
                .build();
    }

    public static EmailRequest createEmailRequest(String email){
        return EmailRequest.builder()
                .email(email)
                .build();
    }

    public static DeactivateRequest createDeactivateRequest(String password){
        return DeactivateRequest.builder()
                .password(password)
                .build();
    }


    public static MemberProfileResponse createMemberProfileResponse(Long memberId, String nickname, String profileUrl){
        return MemberProfileResponse.builder()
                .memberId(memberId)
                .nickname(nickname)
                .profileUrl(profileUrl)
                .build();
    }


    public static Cookie createRefreshTokenCookie(String refreshToken){
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }


}
