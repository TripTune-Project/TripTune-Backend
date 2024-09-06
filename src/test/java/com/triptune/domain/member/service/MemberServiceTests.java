package com.triptune.domain.member.service;

import com.triptune.domain.member.dto.RefreshTokenRequest;
import com.triptune.domain.member.dto.RefreshTokenResponse;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
public class MemberServiceTests {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Test
    @DisplayName("refreshToken() 성공: access token 갱신")
    void successRefreshToken(){
        // given
        String storedRefreshToken = jwtUtil.createToken("test", refreshExpirationTime);

        Member storedMember = createMemberEntity(storedRefreshToken);
        memberRepository.save(storedMember);

        RefreshTokenRequest request = createRefreshTokenRequest(storedRefreshToken);

        // when
        RefreshTokenResponse response = memberService.refreshToken(request);

        // then
        assertNotNull(response.getAccessToken());
        assertTrue(jwtUtil.validateToken(response.getAccessToken()));

    }

    @Test
    @DisplayName("refreshToken() 실패: refresh token 만료")
    void failExpiredRefreshToken(){
        // given
        String storedRefreshToken = jwtUtil.createToken("test", refreshExpirationTime);

        Member storedMember = createMemberEntity(storedRefreshToken);
        memberRepository.save(storedMember);

        String expiredRefreshToken = jwtUtil.createToken("test", -refreshExpirationTime);
        RefreshTokenRequest request = createRefreshTokenRequest(expiredRefreshToken);

        // when
        CustomJwtUnAuthorizedException fail = assertThrows(CustomJwtUnAuthorizedException.class, () -> memberService.refreshToken(request));

        // then
        assertEquals(fail.getMessage(), ErrorCode.EXPIRED_JWT_TOKEN.getMessage());
        assertEquals(fail.getHttpStatus(), ErrorCode.EXPIRED_JWT_TOKEN.getStatus());
    }

    @Test
    @DisplayName("refreshToken() 실패: 저장된 refresh token 과 불일치")
    void failMisMatchRefreshToken() {
        // given
        String storedRefreshToken = jwtUtil.createToken("test", refreshExpirationTime);

        Member storedMember = createMemberEntity(storedRefreshToken);
        memberRepository.save(storedMember);

        String expiredRefreshToken = jwtUtil.createToken("test", refreshExpirationTime + 1000000L);
        RefreshTokenRequest request = createRefreshTokenRequest(expiredRefreshToken);

        // when
        CustomJwtBadRequestException fail = assertThrows(CustomJwtBadRequestException.class, () -> memberService.refreshToken(request));

        // then
        assertEquals(fail.getMessage(), ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage());
        assertEquals(fail.getHttpStatus(), ErrorCode.MISMATCH_REFRESH_TOKEN.getStatus());
    }


    private Member createMemberEntity(String storedRefreshToken){
        return Member.builder()
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
