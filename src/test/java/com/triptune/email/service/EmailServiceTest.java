package com.triptune.email.service;

import com.triptune.email.EmailTest;
import com.triptune.email.dto.request.VerifyAuthRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.member.repository.MemberRepository;
import com.triptune.global.redis.RedisService;
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
public class EmailServiceTest extends EmailTest {

    @InjectMocks private EmailService emailService;
    @Mock private RedisService redisService;
    @Mock private JavaMailSender javaMailSender;
    @Mock private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일 인증 코드 검사")
    void verifyAuthCode(){
        // given
        VerifyAuthRequest request = createVerifyAuthRequest("member@email.com", "Abcd123");

        when(redisService.getEmailData(any(), anyString())).thenReturn("Abcd123");

        // when, then
        assertDoesNotThrow(() -> emailService.verifyAuthCode(request));
    }

    @Test
    @DisplayName("이메일 인증 코드 검사 시 인증 시간이 만료된 경우")
    void verifyAuthCode_invalid(){
        // given
        VerifyAuthRequest request = createVerifyAuthRequest("member@email.com", "Abcd123");

        when(redisService.getEmailData(any(), anyString())).thenReturn(null);

        // when
        EmailVerifyException fail = assertThrows(EmailVerifyException.class, () -> emailService.verifyAuthCode(request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INVALID_VERIFIED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INVALID_VERIFIED_EMAIL.getMessage());
    }

    @Test
    @DisplayName("이메일 인증 코드 검사 시 코드가 틀린 경우")
    void verifyAuthCode_incorrect(){
        // given
        VerifyAuthRequest request = createVerifyAuthRequest("member@email.com", "Abcd123");

        when(redisService.getEmailData(any(), anyString())).thenReturn("abcd123");

        // when
        EmailVerifyException fail = assertThrows(EmailVerifyException.class, () -> emailService.verifyAuthCode(request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INCORRECT_VERIFIED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INCORRECT_VERIFIED_EMAIL.getMessage());
    }


    @RepeatedTest(10)
    @DisplayName("인증 코드 생성")
    void createAuthCode(){
        // given, when
        String response = emailService.createAuthCode();

        // then
        assertThat(response).isNotNull();
        assertThat(response.length()).isEqualTo(6);
    }

}