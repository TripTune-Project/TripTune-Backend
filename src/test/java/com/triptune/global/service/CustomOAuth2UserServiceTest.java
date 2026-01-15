package com.triptune.global.service;

import com.triptune.global.SocialMemberTest;
import com.triptune.global.security.oauth.CustomOAuth2UserService;
import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.global.util.NicknameGenerator;
import com.triptune.member.entity.Member;
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
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest extends SocialMemberTest {

    @InjectMocks private CustomOAuth2UserService customOAuth2UserService;
    @Mock private SocialMemberRepository socialMemberRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ProfileImageService profileImageService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NicknameGenerator nicknameGenerator;

    @Test
    @DisplayName("소셜 회원 로그인 - 네이버")
    void joinOrLogin_socialLogin_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("naverMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);
        createSocialMember(member, SocialType.NAVER, "naverMember");

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any()))
                .thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);
    }


    @Test
    @DisplayName("소셜 회원 로그인 - 카카오")
    void joinOrLogin_socialLogin_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("kakaoMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);
        createSocialMember(member, SocialType.KAKAO, "kakaoMember");

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any()))
                .thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);
    }

    @Test
    @DisplayName("소셜 회원 로그인 - 네이버, 카카오")
    void joinOrLogin_socialLogin_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);
        createSocialMember(member, SocialType.KAKAO, "kakaoMember");
        createSocialMember(member, SocialType.NAVER, "naverMember");

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any()))
                .thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);
    }

    @Test
    @DisplayName("통합 회원 로그인 - 자체, 네이버")
    void joinOrLogin_integrateLogin_native_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createBothTypeMember(oAuth2UserInfo.getEmail(), profileImage);
        createSocialMember(member, SocialType.NAVER, "naverMember");

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any()))
                .thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

    }

    @Test
    @DisplayName("통합 회원 로그인 - 자체, 카카오")
    void joinOrLogin_integrateLogin_native_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createBothTypeMember(oAuth2UserInfo.getEmail(), profileImage);
        createSocialMember(member, SocialType.KAKAO, "kakaoMember");

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any()))
                .thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

    }

    @Test
    @DisplayName("회원가입 시 회원 통합 - 자체, 네이버")
    void joinOrLogin_integrate_native_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원가입 시 회원 통합 - 자체, 카카오")
    void joinOrLogin_integrate_native_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원가입 시 회원 통합 - 네이버, 카카오")
    void joinOrLogin_integrate_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원가입 시 회원 통합 - 자체, 네이버, 카카오")
    void joinOrLogin_integrate_native_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createBothTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

        verify(socialMemberRepository, times(1)).save(any());
    }



    @Test
    @DisplayName("회원가입 시 신규 회원 생성 - 네이버")
    void joinOrLogin_create_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("naverMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원가입 시 신규 회원 생성 - 카카오")
    void joinOrLogin_create_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("kakaoMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(socialMemberRepository.findBySocialIdAndSocialType(anyString(), any())).thenReturn(Optional.empty());
        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);

        // when
        Member response = customOAuth2UserService.joinOrLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("기존 회원인지 체크 후 기존 회원 통합 - 자체, 네이버")
    void processSocialLogin_integrate_native_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.processSocialLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

    }

    @Test
    @DisplayName("기존 회원인지 체크 후 기존 회원 통합 - 자체, 카카오")
    void processSocialLogin_integrate_native_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.processSocialLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);
    }

    @Test
    @DisplayName("기존 회원인지 체크 후 기존 회원 통합 - 네이버, 카카오")
    void processSocialLogin_integrate_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.processSocialLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

    }

    @Test
    @DisplayName("기존 회원인지 체크 후 기존 회원 통합 - 자체, 네이버, 카카오")
    void processSocialLogin_integrate_native_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createBothTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = customOAuth2UserService.processSocialLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

    }

    @Test
    @DisplayName("기존 회원인지 체크 후 신규 생성 - 네이버")
    void processSocialLogin_create_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("naverMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        createSocialMember(member, SocialType.NAVER, oAuth2UserInfo.getSocialId());

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);

        // when
        Member response = customOAuth2UserService.processSocialLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("기존 회원인지 체크 후 신규 생성 - 카카오")
    void processSocialLogin_create_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("kakaoMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        createSocialMember(member, SocialType.KAKAO, oAuth2UserInfo.getSocialId());

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);

        // when
        Member response = customOAuth2UserService.processSocialLogin(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원 통합 - 자체, 네이버")
    void integrateMember_native_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        // when
        Member response = customOAuth2UserService.integrateMember(member, oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원 통합 - 자체, 카카오")
    void integrateMember_native_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        // when
        Member response = customOAuth2UserService.integrateMember(member, oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원 통합 - 네이버, 카카오")
    void integrateMember_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        // when
        Member response = customOAuth2UserService.integrateMember(member, oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("회원 통합 - 자체, 네이버, 카카오")
    void integrateMember_native_naver_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("member@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createBothTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        // when
        Member response = customOAuth2UserService.integrateMember(member, oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isEqualTo(member.getPassword());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);

        verify(socialMemberRepository, times(1)).save(any());
    }


    @Test
    @DisplayName("신규 소셜 회원 생성 - 네이버")
    void createMember_naver() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createNaverUserInfo("naverMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);

        // when
        Member response = customOAuth2UserService.createMember(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }


    @Test
    @DisplayName("신규 소셜 회원 생성 - 카카오")
    void createMember_kakao() {
        // given
        OAuth2UserInfo oAuth2UserInfo = createKaKaoUserInfo("kakaoMember@email.com");

        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createSocialTypeMember(oAuth2UserInfo.getEmail(), profileImage);

        when(profileImageService.saveDefaultProfileImage()).thenReturn(profileImage);

        // when
        Member response = customOAuth2UserService.createMember(oAuth2UserInfo);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getPassword()).isNull();
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);

        verify(socialMemberRepository, times(1)).save(any());
    }



}