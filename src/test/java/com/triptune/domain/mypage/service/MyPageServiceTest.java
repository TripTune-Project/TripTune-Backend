package com.triptune.domain.mypage.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.mypage.MyPageTest;
import com.triptune.domain.mypage.dto.request.MyPagePasswordRequest;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest extends MyPageTest {

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Test
    @DisplayName("비밀번호 변경")
    void changePassword(){
        // given
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123@", "test123!", "test123!");
        Member member = createMember(1L, "member");

        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodingPassword");

        // when, then
        assertDoesNotThrow(() -> myPageService.changePassword("member", request));

    }

    @Test
    @DisplayName("비밀번호 변경 시 사용자 정보 찾을 수 없어 예외 발생")
    void changePasswordMemberNotFoundException(){
        // given
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123@", "test123!", "test123!");

        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> myPageService.changePassword("member", request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 일치하지 않아 예외 발생")
    void changePasswordIncorrectSavedPassword(){
        // given
        MyPagePasswordRequest request = createMyPagePasswordRequest("incorrect123@", "test123!", "test123!");
        Member member = createMember(1L, "member");

        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        ChangePasswordException fail = assertThrows(ChangePasswordException.class, () -> myPageService.changePassword("member", request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getMessage());
    }

}