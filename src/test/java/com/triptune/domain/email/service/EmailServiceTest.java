package com.triptune.domain.email.service;

import com.triptune.domain.email.dto.VerifyAuthRequest;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.util.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일 인증 코드 검사")
    void verifyAuthCodeTrue(){
        // given
        VerifyAuthRequest request = new VerifyAuthRequest("test@email.com", "Abcd123");

        when(redisUtils.getEmailData(any(), anyString())).thenReturn("Abcd123");

        // when
        boolean response = emailService.verifyAuthCode(request);

        // then
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 코드 검사 시 코드가 틀린 경우")
    void verifyAuthCodeFalse(){
        // given
        VerifyAuthRequest request = new VerifyAuthRequest("test@email.com", "Abcd123");

        when(redisUtils.getEmailData(any(), anyString())).thenReturn("abcd123");

        // when
        boolean response = emailService.verifyAuthCode(request);

        // then
        assertThat(response).isFalse();
    }




    @RepeatedTest(10)
    @DisplayName("인증 코드 생성")
    void createAuthCode(){
        // given, when
        String response = emailService.createAuthCode();

        // then
        System.out.println(response);
        assertThat(response).isNotNull();
        assertThat(response.length()).isEqualTo(6);
    }

}