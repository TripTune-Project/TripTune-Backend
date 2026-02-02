package com.triptune.global.util;

import com.triptune.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NicknameGeneratorTest {

    @InjectMocks private NicknameGenerator nicknameGenerator;
    @Mock private MemberRepository memberRepository;


    @Test
    @DisplayName("닉네임 생성")
    void createNickname() {
        // given
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);

        // when
        String response = nicknameGenerator.createNickname();

        // then
        log.info("닉네임 = {}", response);
        assertThat(response).isNotNull();
        assertThat(response).matches("[가-힣]+[가-힣]+\\d{3}");
    }

    @Test
    @DisplayName("닉네임 생성 시 중복")
    void createNickname_duplicateNickname() {
        // given
        when(memberRepository.existsByNickname(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // when
        String response = nicknameGenerator.createNickname();

        // then
        log.info("닉네임 = {}", response);
        assertThat(response).isNotNull();
        assertThat(response).matches("[가-힣]+[가-힣]+\\d{3}");

        verify(memberRepository, times(3)).existsByNickname(anyString());
    }

    @RepeatedTest(10)
    @DisplayName("랜덤 닉네임 생성")
    void generateRandomNickname() {
        // given
        // when
        String response = NicknameGenerator.generateRandomNickname();

        // then
        log.info("닉네임 = {}", response);
        assertThat(response).isNotNull();
        assertThat(response).matches("[가-힣]+[가-힣]+\\d{3}");
    }

}