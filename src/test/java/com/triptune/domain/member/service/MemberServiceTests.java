package com.triptune.domain.member.service;

import com.triptune.domain.member.dto.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@Slf4j
public class MemberServiceTests {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final String accessToken = "testAccessToken";
    private final String refreshToken = "testRefreshToken";
    private final String passwordToken = "testPasswordToken";
    private final String newPassword = "newPassword123@";


    @Test
    @DisplayName("회원가입 성공")
    void join_success(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);

        // when
        memberService.join(request);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패: 이미 가입한 아이디가 존재할 경우")
    void join_userIdExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any(Member.class));
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_USERID.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_USERID.getMessage());

    }

    @Test
    @DisplayName("회원가입 실패: 이미 가입한 닉네임이 존재할 경우")
    void join_nicknameExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any(Member.class));
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("회원가입 실패: 이미 가입한 닉네임이 존재할 경우")
    void join_emailExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any(Member.class));
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_EMAIL.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());
    }


    @Test
    @DisplayName("로그아웃 성공")
    void logout_success(){
        // given
        Member savedMember = createMember(refreshToken);
        memberRepository.save(savedMember);

        LogoutDTO request = LogoutDTO.builder()
                .userId("test")
                .build();

        // when
        memberService.logout(request, accessToken);

        // then
        verify(memberRepository, times(1)).deleteRefreshToken(request.getUserId());
        verify(redisUtil, times(1)).saveExpiredData(accessToken, "logout", 3600);
    }


    @Test
    @DisplayName("refreshToken 갱신 성공")
    void refreshToken_success(){
        // given
        Claims mockClaims = Jwts.claims().setSubject("test");

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.parseClaims(refreshToken)).thenReturn(mockClaims);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(refreshToken)));
        when(jwtUtil.createToken(anyString(), anyLong())).thenReturn(accessToken);

        RefreshTokenRequest request = createRefreshTokenRequest(refreshToken);

        // when
        RefreshTokenResponse response = memberService.refreshToken(request);

        // then
        assertNotNull(response.getAccessToken());

    }


    @Test
    @DisplayName("refreshToken 갱신 실패: 저장된 refresh token 와 요청 refresh token이 불일치하는 경우")
    void misMatchRefreshToken_customJwtBadRequestException() {
        // given
        String savedRefreshToken = "refreshTokenInDatabase";
        Claims mockClaims = Jwts.claims().setSubject("test");

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.parseClaims(refreshToken)).thenReturn(mockClaims);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(savedRefreshToken)));

        RefreshTokenRequest request = createRefreshTokenRequest(refreshToken);

        // when
        CustomJwtBadRequestException fail = assertThrows(CustomJwtBadRequestException.class, () -> memberService.refreshToken(request));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.MISMATCH_REFRESH_TOKEN.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success(){
        // given
        String email = "test@email.com";
        String encodedPassword = "encodedPassword";

        Member storedMember = createMember(refreshToken);

        when(redisUtil.getData(anyString())).thenReturn(email);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(storedMember));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);


        // when
        memberService.changePassword(createChangePasswordDTO());

        // then
        verify(redisUtil, times(1)).getData(passwordToken);
        verify(memberRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(newPassword);
        assertEquals(encodedPassword, storedMember.getPassword());

        log.info("인코딩 제공 비밀번호 : {}, 저장된 비밀번호 : {}", encodedPassword, storedMember.getPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 비밀번호 변경 토큰 유효 시간이 만료되어 ChangePasswordException 발생")
    void changePassword_changePasswordException(){
        // given
        when(redisUtil.getData(anyString())).thenReturn(null);

        // when
        ChangePasswordException fail = assertThrows(ChangePasswordException.class, () -> memberService.changePassword(createChangePasswordDTO()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.INVALID_CHANGE_PASSWORD.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.INVALID_CHANGE_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 사용자 정보를 찾을 수 없어 DataNotFoundException 발생")
    void changePassword_dataNotFoundException(){
        // given
        String email = "test@email.com";

        when(redisUtil.getData(anyString())).thenReturn(email);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> memberService.changePassword(createChangePasswordDTO()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());
    }


    private MemberRequest createMemberRequest(){
        return MemberRequest.builder()
                .userId("testUser")
                .password("password123@")
                .repassword("password123@")
                .nickname("test")
                .email("test@test.com")
                .build();
    }

    /**
     * Member 객체 제공 메서드
     * @param storedRefreshToken : 저장된 refresh token
     * @return Member
     */
    private Member createMember(String storedRefreshToken){
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

    /**
     * RefreshTokenRequest 객체 제공 메서드 (토큰 갱신 요청 dto)
     * @param refreshToken : 기존에 사용하던 refresh token
     * @return Refresh Token
     */
    private RefreshTokenRequest createRefreshTokenRequest(String refreshToken){
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * ChangePasswordTO 객체 제공 메서드 (비밀번호 변경 요청 dto)
     * @return ChangePasswordDTO
     */
    private ChangePasswordDTO createChangePasswordDTO(){
        return ChangePasswordDTO.builder()
                .passwordToken(passwordToken)
                .password(newPassword)
                .repassword(newPassword)
                .build();
    }
}
