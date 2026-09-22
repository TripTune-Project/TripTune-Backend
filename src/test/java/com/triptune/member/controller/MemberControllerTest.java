package com.triptune.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.redis.RedisService;
import com.triptune.global.security.CookieType;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.security.jwt.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.util.CookieUtils;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.entity.Member;
import com.triptune.member.exception.FailLoginException;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.exception.InvalidPasswordResetTokenException;
import com.triptune.member.exception.UnsupportedSocialMemberException;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.service.MemberService;
import com.triptune.member.service.dto.LoginResult;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.triptune.member.fixture.MemberFixture.createLoginRequest;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtUtils jwtUtils;
    @MockBean private CookieUtils cookieUtils;
    @MockBean private RedisService redisService;
    @MockBean private MemberService memberService;
    @MockBean private JwtAuthFilter jwtAuthFilter;



    @Test
    @DisplayName("회원가입")
    void join() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        // when
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(memberService).join(any(JoinRequest.class));
    }


    @ParameterizedTest
    @DisplayName("회원가입 시 이메일 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankEmail(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                input,
                "password12!@",
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 이메일 null 값이 들어와 400 반환")
    void join_invalidNullEmail() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                null,
                "password12!@",
                "password12!@",
                "nickname"
        );


        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("회원가입 시 이메일 형식에 맞지 않아 400 반환")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void join_invalidEmail(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                input,
                "password12!@",
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }


    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankPassword(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                input,
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호 null 값이 들어와 400 반환")
    void join_invalidNullPassword() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                null,
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void join_invalidPassword(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                input,
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 재입력 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankRePassword(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                input,
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호 재입력 null 값이 들어와 400 반환")
    void join_invalidNullRePassword() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                null,
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 재입력 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void join_invalidRePassword(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                input,
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @ParameterizedTest
    @DisplayName("회원가입 시 닉네임 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankNickname(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 닉네임 null 값이 들어와 400 반환")
    void join_invalidNullNickname() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                null
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 닉네임 입력값 검사로 400 반환")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@"})
    void join_invalidNickname(String input) throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호, 비밀번호 재입력 불일치로 인한 400 반환")
    void join_incorrectPasswordAndRePassword() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "repassword12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("회원가입 시 이미 존재하는 이메일로 인해 409 반환")
    void join_existedEmail() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        willThrow(new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL))
                .given(memberService).join(any(JoinRequest.class));

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));
    }

    @Test
    @DisplayName("회원가입 시 인증되지 않은 이메일로 400 반환")
    void join_notVerifiedEmail() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        willThrow(new EmailVerifyException(ErrorCode.NOT_VERIFIED_EMAIL))
                .given(memberService).join(any(JoinRequest.class));

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));
    }


    @Test
    @DisplayName("일반 회원 로그인")
    void login() throws Exception {
        // given
        LoginRequest request = MemberFixture.createLoginRequest("member@email.com", "password12!@");
        LoginResult response = MemberFixture.createLoginResult(
                "testAccessToken",
                "testRefreshToken",
                "닉네임"
        );

        given(memberService.login(any(LoginRequest.class))).willReturn(response);

        // when
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").value("testAccessToken"))
                .andExpect(jsonPath("$.data.nickname").value("닉네임"));

        // then
        verify(memberService).login(any(LoginRequest.class));
        verify(cookieUtils).createCookie(any(CookieType.class), anyString());
    }


    @ParameterizedTest
    @DisplayName("로그인 시 이메일 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void login_invalidNotBlankEmail(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest(input, "password12!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 시 이메일 null 값이 들어와 400 반환")
    void login_invalidNullEmail() throws Exception {
        // given
        LoginRequest request = createLoginRequest(null, "password12!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("로그인 시 이메일 형식에 맞지 않아 400 반환")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void login_invalidEmail(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest(input, "password12!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }


    @ParameterizedTest
    @DisplayName("로그인 시 비밀번호 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void login_invalidNotBlankPassword(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest("member@email.com", input);

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 시 비밀번호 null 값이 들어와 400 반환")
    void login_invalidNullPassword() throws Exception {
        // given
        LoginRequest request = createLoginRequest("member@email.com", null);

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("로그인 시 이메일 맞지 않아 400 반환")
    void login_incorrectEmail() throws Exception {
        // given
        LoginRequest request = createLoginRequest("fail@email.com", "password12!@");

        willThrow(new FailLoginException(ErrorCode.FAILED_LOGIN))
                .given(memberService).login(any(LoginRequest.class));

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 400 반환")
    void login_incorrectPassword() throws Exception {
        // given
        LoginRequest request = MemberFixture.createLoginRequest("member@email.com", "fail!@");

        willThrow(new FailLoginException(ErrorCode.FAILED_LOGIN))
                .given(memberService).login(any(LoginRequest.class));

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }


    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        // given
        String accessToken = "testAccessToken";
        LogoutRequest request = MemberFixture.createLogoutRequest("닉네임12");

        given(jwtUtils.resolveToken(any(HttpServletRequest.class)))
                .willReturn(accessToken);

        // when
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(jwtUtils).resolveToken(any(HttpServletRequest.class));
        verify(memberService).logout(any(LogoutRequest.class), eq(accessToken));
        verify(cookieUtils).deleteAllCookies(any(HttpServletResponse.class));
    }


    @ParameterizedTest
    @DisplayName("로그아웃 시 닉네임 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void logout_invalidNotBlankNickname(String input) throws Exception {
        // given
        String accessToken = "testAccessToken";
        LogoutRequest request = MemberFixture.createLogoutRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));

    }

    @Test
    @DisplayName("로그아웃 시 닉네임 null 값이 들어와 400 반환")
    void logout_invalidNullNickname() throws Exception {
        // given
        String accessToken = "testAccessToken";
        LogoutRequest request = MemberFixture.createLogoutRequest(null);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("로그아웃 시 닉네임 입력값 검사로 400 반환")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@", "12345", "행복1"})
    void logout_invalidNickname(String input) throws Exception {
        // given
        String accessToken = "testAccessToken";
        LogoutRequest request = MemberFixture.createLogoutRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }


    @Test
    @DisplayName("로그아웃 시 회원 데이터 없어 404 반환")
    void logout_memberNotFound() throws Exception {
        // given
        String accessToken = "testAccessToken";
        LogoutRequest request = MemberFixture.createLogoutRequest("notMember");

        given(jwtUtils.resolveToken(any(HttpServletRequest.class)))
                .willReturn(accessToken);
        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).logout(any(LogoutRequest.class), anyString());

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신")
    void refreshToken() throws Exception {
        // given
        String refreshToken = "testRefreshToken";
        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);
        RefreshTokenResponse response = MemberFixture.createRefreshTokenResponse("testAccessToken");

        given(cookieUtils.getRefreshTokenFromCookie(any(HttpServletRequest.class)))
                .willReturn(Optional.of(refreshToken));
        given(memberService.refreshToken(anyString()))
                .willReturn(response);

        // when
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // then
        verify(cookieUtils).getRefreshTokenFromCookie(any(HttpServletRequest.class));
        verify(memberService).refreshToken(eq(refreshToken));
    }

    @Test
    @DisplayName("토큰 갱신 시 쿠키 존재하지 않아 401 반환")
    void refreshToken_noCookie() throws Exception {
        // given, when, then
        mockMvc.perform(post("/api/members/refresh"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 refresh token 만료로 401 반환")
    void refreshToken_expired() throws Exception {
        // given
        String refreshToken = "ExpiredRefreshToken";
        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);

        given(cookieUtils.getRefreshTokenFromCookie(any(HttpServletRequest.class)))
                .willReturn(Optional.of(refreshToken));
        willThrow(new CustomJwtUnAuthorizedException(ErrorCode.EXPIRED_JWT_TOKEN))
                .given(memberService).refreshToken(anyString());

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 회원 데이터 존재하지 않아 404 반환")
    void refreshToken_memberNotFound() throws Exception {
        // given
        String refreshToken = "testRefreshToken";
        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);

        given(cookieUtils.getRefreshTokenFromCookie(any(HttpServletRequest.class)))
                .willReturn(Optional.of(refreshToken));
        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).refreshToken(anyString());

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("토큰 갱신 시 요청 refreshToken 과 저장된 refresh token 값이 달라 401 반환")
    void refreshToken_NotEqualsRefreshToken() throws Exception {
        // given
        String refreshToken = "testRefreshToken";
        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);

        given(cookieUtils.getRefreshTokenFromCookie(any(HttpServletRequest.class)))
                .willReturn(Optional.of(refreshToken));
        willThrow(new CustomJwtUnAuthorizedException(ErrorCode.MISMATCH_REFRESH_TOKEN))
                .given(memberService).refreshToken(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 찾기")
    void findPassword() throws Exception {
        // given
        FindPasswordRequest request = MemberFixture.createFindPasswordRequest("member@email.com");

        // when
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(memberService).findPassword(any(FindPasswordRequest.class));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 찾기 시 이메일 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void findPassword_invalidNotBlankEmail(String input) throws Exception {
        // given
        FindPasswordRequest request = MemberFixture.createFindPasswordRequest(input);

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 찾기 시 이메일 null 값이 들어와 400 반환")
    void findPassword_invalidNullEmail() throws Exception {
        // given
        FindPasswordRequest request = MemberFixture.createFindPasswordRequest(null);

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 찾기 시 이메일 형식에 맞지 않아 400 반환")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void findPassword_invalidEmail(String input) throws Exception {
        // given
        FindPasswordRequest request = MemberFixture.createFindPasswordRequest(input);

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 찾기 시 회원 데이터 존재하지 않아 404 반환")
    void findPassword_memberNotFound() throws Exception {
        // given
        FindPasswordRequest request = MemberFixture.createFindPasswordRequest("notMember@email.com");

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).findPassword(any(FindPasswordRequest.class));

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("비밀번호 초기화")
    void resetPassword() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                "password12!@"
        );

        given(redisService.getData(anyString())).willReturn("member@email.com");

        // when
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(memberService).resetPassword(any(ResetPasswordRequest.class));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 변경 토큰 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void resetPassword_invalidNotBlankPasswordToken(String input) throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                input,
                "password12!@",
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 변경 토큰은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 변경 토큰 null 값이 들어와 400 반환")
    void resetPassword_invalidNullPasswordToken() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                null,
                "password12!@",
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 변경 토큰은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void resetPassword_invalidNotBlankPassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                input,
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 null 값이 들어와 400 반환")
    void resetPassword_invalidNullPassword() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                null,
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void resetPassword_invalidPassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                input,
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 재입력 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void resetPassword_invalidNotBlankRePassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 재입력 null 값이 들어와 400 반환")
    void resetPassword_invalidNullRePassword() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                null
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 재입력 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void resetPassword_invalidRePassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호와 재입력 비밀번호가 달라 400 반환")
    void resetPassword_notMatchPassword() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                "notPassword34!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 저장된 비밀번호 변경 토큰이 존재하지 않아 400 반환")
    void resetPassword_passwordTokenNotFound() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                "password12!@"
        );

        willThrow(new InvalidPasswordResetTokenException(ErrorCode.INVALID_CHANGE_PASSWORD_TOKEN))
                .given(memberService).resetPassword(any(ResetPasswordRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CHANGE_PASSWORD_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 회원 데이터 존재하지 않아 404 반환")
    void resetPassword_memberNotFound() throws Exception {
        // given
        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest(
                "changePassword",
                "password12!@",
                "password12!@"
        );

        given(redisService.getData(anyString())).willReturn("noMember@email.com");

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).resetPassword(any(ResetPasswordRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경")
    void changePassword_nativeMember() throws Exception {
        // given
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                ProfileImageFixture.createProfileImage("memberImage")
        );
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "test123@",
                "test123!",
                "test123!"
        );

        // when
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(memberService).changePassword(eq(member.getMemberId()), any(ChangePasswordRequest.class));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 현재 비밀번호 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankNowPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                input,
                "password12!@",
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("현재 비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호 null 값이 들어와 400 반환")
    void changePassword_invalidNullNowPassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                null,
                "password12!@",
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("현재 비밀번호는 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                input,
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 null 값이 들어와 400 반환")
    void changePassword_invalidNullPassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                null,
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void changePassword_invalidPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                input,
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankRePassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 null 값이 들어와 400 반환")
    void changePassword_invalidNullRePassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                "password12!@",
                null
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void changePassword_invalidRePassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @Test
    @DisplayName("비밀번호 변경 시 변경 비밀번호와 재입력 비밀번호가 일치하지 않아 400 반환")
    void changePassword_passwordMismatch() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!",
                "password12!@",
                "test456!"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호와 변경 비밀번호가 같아 400 반환")
    void changePassword_sameNowPasswordAndNewPassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!@",
                "password12!@",
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 회원 정보를 찾을 수 없어 404 반환")
    void changePassword_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member notMember = MemberFixture.createNativeTypeMemberWithId(
                1000L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(notMember);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!",
                "password12!@",
                "password12!@"
        );

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).changePassword(anyLong(), any(ChangePasswordRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 소셜 회원으로 400 반환")
    void changePassword_socialMember() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createSocialTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!",
                "password12!@",
                "password12!@"
        );

        willThrow(new UnsupportedSocialMemberException(ErrorCode.SOCIAL_MEMBER_PASSWORD_CHANGE_NOT_ALLOWED))
                .given(memberService).changePassword(anyLong(), any(ChangePasswordRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_MEMBER_PASSWORD_CHANGE_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 현재 비밀번호가 일치하지 않아 400 반환")
    void changePassword_incorrectSavedPassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "notPassword12!",
                "password12!@",
                "password12!@"
        );

        willThrow(new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD))
                .given(memberService).changePassword(anyLong(), any(ChangePasswordRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }


    @Test
    @DisplayName("회원 정보 조회")
    void getMemberInfo() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        String profileImageUrl = "http://test.com/memberImage";
        MemberInfoResponse response = MemberFixture.createMemberInfoResponse(member, profileImageUrl);

        given(memberService.getMemberInfo(anyLong())).willReturn(response);

        // when
        mockMvc.perform(get("/api/members/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImageUrl));

        // then
        verify(memberService).getMemberInfo(eq(member.getMemberId()));
    }

    @Test
    @DisplayName("회원 정보 조회 시 회원 데이터 없어 404 반환")
    void getMemberInfo_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member notMember = MemberFixture.createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(notMember);

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).getMemberInfo(anyLong());

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("회원 닉네임 변경")
    void changeNickname() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest("newNickname");

        // when
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(memberService).changeNickname(eq(member.getMemberId()), any(ChangeNicknameRequest.class));
    }


    @ParameterizedTest
    @DisplayName("닉네임 변경 시 닉네임 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void changeNickname_invalidNotBlankNickname(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("닉네임 변경 시 닉네임 null 값이 들어와 400 반환")
    void changeNickname_invalidNullNickname() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest(null);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("닉네임 변경 시 닉네임 입력값 검사로 400 반환")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@"})
    void changeNickname_invalidNickname(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }

    @Test
    @DisplayName("닉네임 변경 시 회원 데이터 없어 404 반환")
    void changeNickname_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member notMember = MemberFixture.createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(notMember);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest("newNickname");

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).changeNickname(anyLong(), any(ChangeNicknameRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원 닉네임 변경 시 이미 존재하는 닉네임으로 409 반환")
    void changeNickname_dataExist() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest(member.getNickname());

        willThrow(new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME))
                .given(memberService).changeNickname(anyLong(), any(ChangeNicknameRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage()));
    }


    @Test
    @DisplayName("이메일 변경")
    void changeEmail() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest("changeEmail@email.com");

        given(redisService.getEmailData(any(), anyString()))
                .willReturn("true");

        // when
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(memberService).changeEmail(eq(member.getMemberId()), any(EmailRequest.class));
    }


    @ParameterizedTest
    @DisplayName("이메일 변경 시 이메일 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void changeEmail_invalidNotBlankEmail(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("이메일 변경 시 이메일 null 값이 들어와 400 반환")
    void changeEmail_invalidNullEmail() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest(null);

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("이메일 변경 시 이메일 형식에 맞지 않아 400 반환")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void changeEmail_invalidEmail(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("이메일 변경 시 이미 존재하는 이메일로 409 반환")
    void changeEmail_duplicateEmail() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(1L, "member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest("member@email.com");

        willThrow(new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL))
                .given(memberService).changeEmail(anyLong(), any(EmailRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));
    }

    @Test
    @DisplayName("이메일 변경 시 인증되지 않은 이메일로 400 반환")
    void changeEmail_notVerifiedEmail() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest("changeEmail@email.com");

        given(redisService.getEmailData(any(), anyString()))
                .willReturn(null);
        willThrow(new EmailVerifyException(ErrorCode.NOT_VERIFIED_EMAIL))
                .given(memberService).changeEmail(anyLong(), any(EmailRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));
    }

    @Test
    @DisplayName("이메일 변경 시 회원 데이터 없어 404 반환")
    void changeEmail_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member notMember = MemberFixture.createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(notMember);

        EmailRequest request = MemberFixture.createEmailRequest("changeEmail@email.com");

        given(redisService.getEmailData(any(), anyString())).willReturn("true");

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).changeEmail(anyLong(), any(EmailRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @ParameterizedTest
    @DisplayName("회원 북마크 조회 시 정렬 파라미터 잘못된 값이 들어와 400 반환")
    @ValueSource(strings = {"ne", "@n", " ", ""})
    void getMemberBookmarks_IllegalSortType(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", input))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deactivateMember() throws Exception {
        // given
        String accessToken = "testAccessToken";
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest("password12!@");

        given(jwtUtils.resolveToken(any(HttpServletRequest.class)))
                .willReturn(accessToken);

        // when
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(jwtUtils).resolveToken(any(HttpServletRequest.class));
        verify(memberService).deactivateMember(eq(member.getMemberId()), eq(accessToken), any(DeactivateRequest.class));
        verify(cookieUtils).deleteAllCookies(any(HttpServletResponse.class));
    }

    @ParameterizedTest
    @DisplayName("회원 탈퇴 시 비밀번호 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void deactivateMember_invalidNotBlankPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호 null 값이 들어와 400 반환")
    void deactivateMember_invalidNullPassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(null);

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원 탈퇴 시 비밀번호 유효성 검사로 400 반환")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void deactivateMember_invalidPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 시 회원 데이터를 찾을 수 없어 404 반환")
    void deactivateMember_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member notMember = MemberFixture.createNativeTypeMemberWithId(
                1000L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(notMember);

        DeactivateRequest request = MemberFixture.createDeactivateRequest("test123@");

        given(jwtUtils.resolveToken(any(HttpServletRequest.class)))
                .willReturn("testAccessToken");
        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(memberService).deactivateMember(anyLong(), anyString(), any(DeactivateRequest.class));


        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("소셜 회원 탈퇴 요청으로 400 반환")
    void deactivateMember_socialMember() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createSocialTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest("test123@");

        given(jwtUtils.resolveToken(any(HttpServletRequest.class)))
                .willReturn("testAccessToken");
        willThrow(new UnsupportedSocialMemberException(ErrorCode.SOCIAL_MEMBER_DEACTIVATE_NOT_ALLOWED))
                .given(memberService).deactivateMember(anyLong(), anyString(), any(DeactivateRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_MEMBER_DEACTIVATE_NOT_ALLOWED.getMessage()));
    }



    @Test
    @DisplayName("회원 탈퇴 시 비밀번호가 맞지 않아 400 반환")
    void deactivateMember_incorrectPassword() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );
        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest("incorrect12!@");

        given(jwtUtils.resolveToken(any(HttpServletRequest.class)))
                .willReturn("testAccessToken");
        willThrow(new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD))
                .given(memberService).deactivateMember(anyLong(), anyString(), any(DeactivateRequest.class));

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }

}
