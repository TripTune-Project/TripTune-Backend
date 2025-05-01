package com.triptune.global.service;

import com.triptune.global.SocialMemberTest;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.security.oauth.CustomOAuth2UserService;
import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.JoinType;
import com.triptune.member.enums.SocialType;
import com.triptune.member.repository.MemberRepository;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest extends SocialMemberTest {

    @InjectMocks private CustomOAuth2UserService customOAuth2UserService;
    @Mock private SocialMemberRepository socialMemberRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ProfileImageService profileImageService;

    @Test
    @DisplayName("소셜 회원 정보가 저장되어 있는 경우 - 네이버")
    void getOrCreateSocialMember_get_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com", "member");

        ProfileImage profileImage = createProfileImage(1L, "image");
        Member member = createMember(1L, "member@email.com", JoinType.SOCIAL, profileImage);
        SocialMember socialMember = createSocialMember(1L, member, oAuth2UserInfo.getSocialId(), SocialType.NAVER);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.getOrCreateSocialMember(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(oAuth2UserInfo.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(oAuth2UserInfo.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);
    }

    @Test
    @DisplayName("소셜 회원 생성 - 네이버")
    void getOrCreateSocialMember_create_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com", "member");

        ProfileImage profileImage = createProfileImage(1L, "image");
        Member member = createMember(1L, "member@email.com", JoinType.SOCIAL, profileImage);
        SocialMember socialMember = createSocialMember(1L, member, oAuth2UserInfo.getSocialId(), SocialType.NAVER);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);
        when(memberRepository.save(any())).thenReturn(member);

        // when
        Member response = customOAuth2UserService.getOrCreateSocialMember(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(oAuth2UserInfo.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(oAuth2UserInfo.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);
    }

    @Test
    @DisplayName("소셜 사용자 추가 시 이메일 중복으로 예외 발생")
    void createSocialMember_duplicateEmail() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com", "member");

        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class,
                () -> customOAuth2UserService.createSocialMember(oAuth2UserInfo));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());
    }

    @Test
    @DisplayName("소셜 사용자 추가 시 닉네임 중복으로 예외 발생")
    void createSocialMember_duplicateNickname() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com", "member");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class,
                () -> customOAuth2UserService.createSocialMember(oAuth2UserInfo));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());
    }

}