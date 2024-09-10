package com.triptune.domain.member.service;

import com.triptune.domain.member.dto.RefreshTokenRequest;
import com.triptune.domain.member.dto.RefreshTokenResponse;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Transactional
public class MemberServiceTests {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtil jwtUtil;

    private String refreshToken = "testRefreshToken";
    private String accessToken = "testAccessToken";

    @Test
    @DisplayName("refreshToken() 성공: access token 갱신")
    void refreshToken_success(){
        // given
        Claims mockClaims = Jwts.claims().setSubject("test");

        when(jwtUtil.validateToken(refreshToken))
                .thenReturn(true);

        when(jwtUtil.parseClaims(refreshToken))
                .thenReturn(mockClaims);

        when(memberRepository.findByUserId(any()))
                .thenReturn(Optional.of(createMemberEntity(refreshToken)));

        when(jwtUtil.createToken(anyString(), anyLong()))
                .thenReturn(accessToken);

        RefreshTokenRequest request = createRefreshTokenRequest(refreshToken);

        // when
        RefreshTokenResponse response = memberService.refreshToken(request);

        // then
        assertNotNull(response.getAccessToken());

    }


    @Test
    @DisplayName("refreshToken() 실패: 저장된 refresh token 와 요청 refresh token이 불일치하는 경우")
    void misMatchRefreshToken_fail() {
        // given
        String savedRefreshToken = "refreshTokenInDatabase";
        Claims mockClaims = Jwts.claims().setSubject("test");

        when(jwtUtil.validateToken(refreshToken))
                .thenReturn(true);

        when(jwtUtil.parseClaims(refreshToken))
                .thenReturn(mockClaims);

        when(memberRepository.findByUserId(any()))
                .thenReturn(Optional.of(createMemberEntity(savedRefreshToken)));

        RefreshTokenRequest request = createRefreshTokenRequest(refreshToken);

        // when
        CustomJwtBadRequestException fail = assertThrows(CustomJwtBadRequestException.class, () -> memberService.refreshToken(request));

        // then
        assertEquals(fail.getMessage(), ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage());
        assertEquals(fail.getHttpStatus(), ErrorCode.MISMATCH_REFRESH_TOKEN.getStatus());
    }


    private Member createMemberEntity(String storedRefreshToken){
        return Member.builder()
                .memberId(1L)
                .userId("test")
                .email("test@email.com")
                .password("test123@")
                .nickname("test")
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .refreshToken(storedRefreshToken)
                .build();
    }

    private RefreshTokenRequest createRefreshTokenRequest(String refreshToken){
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }
}
