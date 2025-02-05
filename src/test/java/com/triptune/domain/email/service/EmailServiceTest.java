package com.triptune.domain.email.service;

import com.triptune.domain.email.dto.VerifyAuthRequest;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.util.RedisUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일 인증 코드 검사")
    void verifyAuthCodeTrue(){
        // given
        VerifyAuthRequest request = new VerifyAuthRequest("test@email.com", "Abcd123");

        when(redisUtil.getData(anyString())).thenReturn("Abcd123");

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

        when(redisUtil.getData(anyString())).thenReturn("abcd123");

        // when
        boolean response = emailService.verifyAuthCode(request);

        // then
        assertThat(response).isFalse();
    }

    @Test
    @DisplayName("이메일 유효성 검증")
    void validateEmail(){
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisUtil.existData(anyString())).thenReturn(false);

        // when
        assertDoesNotThrow(() -> emailService.validateEmail("test@email.com"));
    }

    @Test
    @DisplayName("이메일 유효성 검증 시 존재하는 이메일로 예외 발생")
    void validateEmail_alreadyExistedEmail(){
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> emailService.validateEmail("test@email.com"));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());

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