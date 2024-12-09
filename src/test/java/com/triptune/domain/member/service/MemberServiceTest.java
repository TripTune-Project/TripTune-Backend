package com.triptune.domain.member.service;

import com.triptune.domain.member.MemberTest;
import com.triptune.domain.member.dto.LogoutDTO;
import com.triptune.domain.member.dto.request.LoginRequest;
import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.member.dto.request.RefreshTokenRequest;
import com.triptune.domain.member.dto.response.LoginResponse;
import com.triptune.domain.member.dto.response.RefreshTokenResponse;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest extends MemberTest {

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

    private final String accessToken = "MemberAccessToken";
    private final String refreshToken = "MemberRefreshToken";
    private final String passwordToken = "MemberPasswordToken";

    private Member member;

    @BeforeEach
    void setUp(){
        member = createMember(null, "member");
    }


    @Test
    @DisplayName("회원가입")
    void join(){
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
    @DisplayName("회원가입 시 이미 가입한 아이디가 존재해 예외 발생")
    void join_userIdExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any());
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_USERID.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_USERID.getMessage());

    }

    @Test
    @DisplayName("회원가입 시 이미 가입한 닉네임이 존재해 예외 발생")
    void join_nicknameExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any());
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("회원가입 시 이미 가입한 닉네임이 존재해 예외 발생")
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
    @DisplayName("로그인")
    void login(){
        // given
        LoginRequest loginRequest = createLoginRequest(member.getUserId(), passwordToken);

        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.createToken(anyString(), anyLong())).thenReturn(accessToken);
        when(jwtUtil.createToken(anyString(), anyLong())).thenReturn(refreshToken);

        // when
        LoginResponse response = memberService.login(loginRequest);

        // then
        assertEquals(response.getNickname(), member.getNickname());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    @DisplayName("로그인 시 사용자 데이터 없어 예외 발생")
    void loginNotFoundUser_failLoginException(){
        // given
        LoginRequest loginRequest = createLoginRequest(member.getUserId(), passwordToken);

        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        // when
        FailLoginException fail = assertThrows(FailLoginException.class, () -> memberService.login(loginRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FAILED_LOGIN.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FAILED_LOGIN.getMessage());
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void loginMismatchPassword_failLoginException(){
        // given
        LoginRequest loginRequest = createLoginRequest(member.getUserId(), passwordToken);

        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        FailLoginException fail = assertThrows(FailLoginException.class, () -> memberService.login(loginRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FAILED_LOGIN.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FAILED_LOGIN.getMessage());
    }


    @Test
    @DisplayName("로그아웃")
    void logout(){
        // given
        LogoutDTO request = createLogoutDTO(member.getNickname());

        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        memberService.logout(request, accessToken);

        // then
        verify(memberRepository, times(1)).deleteRefreshTokenByNickname(request.getNickname());
        verify(redisUtil, times(1)).saveExpiredData(accessToken, "logout", 3600);
    }

    @Test
    @DisplayName("로그아웃 요청 시 사용자 데이터 없어 예외 발생")
    void logout_dataNotFoundException(){
        // given
        LogoutDTO request = createLogoutDTO("notMember");

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> memberService.logout(request, accessToken));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("refresh token 갱신 성공")
    void refreshToken(){
        // given
        Claims mockClaims = Jwts.claims().setSubject("test");

        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.parseClaims(anyString())).thenReturn(mockClaims);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member));
        when(jwtUtil.createToken(anyString(), anyLong())).thenReturn(accessToken);

        RefreshTokenRequest request = createRefreshTokenRequest(refreshToken);

        // when
        RefreshTokenResponse response = memberService.refreshToken(request);

        // then
        assertNotNull(response.getAccessToken());

    }


    @Test
    @DisplayName("토큰 갱신 시 저장된 refresh token 과 요청 refresh token 이 불일치해 예외 발생")
    void misMatchRefreshToken_customJwtBadRequestException() {
        // given
        String notEqualRefreshToken = "NotEqualRefreshToken";
        Claims mockClaims = Jwts.claims().setSubject("test");

        RefreshTokenRequest request = createRefreshTokenRequest(notEqualRefreshToken);

        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.parseClaims(anyString())).thenReturn(mockClaims);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member));

        // when
        CustomJwtBadRequestException fail = assertThrows(CustomJwtBadRequestException.class, () -> memberService.refreshToken(request));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.MISMATCH_REFRESH_TOKEN.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경")
    void changePassword(){
        // given
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";

        when(redisUtil.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);


        // when
        memberService.changePassword(createChangePasswordDTO(passwordToken, newPassword));

        // then
        verify(redisUtil, times(1)).getData(passwordToken);
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        verify(passwordEncoder, times(1)).encode(newPassword);
        assertEquals(encodedPassword, member.getPassword());

        System.out.println("인코딩 제공 비밀번호 : " + encodedPassword + " 저장된 비밀번호 : " + member.getPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 토큰 유효 시간이 만료되어 예외 발생")
    void changePassword_changePasswordException(){
        // given
        String newPassword = "newPassword";
        when(redisUtil.getData(anyString())).thenReturn(null);

        // when
        ChangePasswordException fail = assertThrows(ChangePasswordException.class,
                () -> memberService.changePassword(createChangePasswordDTO(accessToken, newPassword)));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.INVALID_CHANGE_PASSWORD.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.INVALID_CHANGE_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 시 사용자 정보를 찾을 수 없어 얘외 발생")
    void changePassword_dataNotFoundException(){
        // given
        String newPassword = "newPassword";

        when(redisUtil.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.changePassword(createChangePasswordDTO(accessToken, newPassword)));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사용자 중복 체크")
    void validateUniqueMemberInfo(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);


        // when, then
        assertDoesNotThrow(() -> memberService.validateUniqueMemberInfo(request));

    }

    @Test
    @DisplayName("사용자 중복 체크 시 이미 가입한 아이디가 존재해 예외 발생")
    void validateUniqueMemberInfo_userIdDataExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.validateUniqueMemberInfo(request));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_USERID.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_USERID.getMessage());

    }

    @Test
    @DisplayName("사용자 중복 체크 시 이미 가입한 닉네임이 존재해 예외 발생")
    void validateUniqueMemberInfo_nicknameDataExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.validateUniqueMemberInfo(request));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());

    }

    @Test
    @DisplayName("사용자 중복 체크 시 이미 가입한 이메일이 존재해 예외 발생")
    void validateUniqueMemberInfo_emailDataExistException(){
        // given
        MemberRequest request = createMemberRequest();

        when(memberRepository.existsByUserId(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.validateUniqueMemberInfo(request));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_EXISTED_EMAIL.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());

    }

    @Test
    @DisplayName("이메일을 이용해 저장된 사용자 정보 조회")
    void getMemberByEmail(){
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        Member response = memberService.getMemberByEmail(member.getEmail());

        // then
        assertEquals(response.getUserId(), member.getUserId());
        assertEquals(response.getEmail(), member.getEmail());

    }

    @Test
    @DisplayName("이메일로 사용자 정보 조회 시 데이터 없어 예외 발생")
    void getMemberByEmail_dataNotFoundException(){
        // given
        String email = "test@email.com";

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.getMemberByEmail(email));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());
    }



}
