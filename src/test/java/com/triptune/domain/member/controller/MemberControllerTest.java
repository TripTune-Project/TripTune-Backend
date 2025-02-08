package com.triptune.domain.member.controller;

import com.triptune.global.service.S3Service;
import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.MemberTest;
import com.triptune.domain.member.dto.request.ChangeNicknameRequest;
import com.triptune.domain.member.dto.request.ChangePasswordRequest;
import com.triptune.domain.member.dto.request.JoinRequest;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.profile.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class MemberControllerTest extends MemberTest {
    private final WebApplicationContext wac;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final PasswordEncoder passwordEncoder;

    @MockBean
    private RedisUtil redisUtil;

    @MockBean
    private EmailService emailService;

    @MockBean
    private S3Service s3Service;

    private MockMvc mockMvc;

    @Autowired
    public MemberControllerTest(WebApplicationContext wac, JwtUtil jwtUtil, MemberRepository memberRepository, ProfileImageRepository profileImageRepository, PasswordEncoder passwordEncoder) {
        this.wac = wac;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.profileImageRepository = profileImageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("회원가입")
    void join() throws Exception {
        when(redisUtil.getEmailData(any(), anyString())).thenReturn("true");

        mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createMemberRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));;
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호 유효성 검사로 예외 발생")
    void joinInvalidPassword_methodArgumentNotValidException() throws Exception {
        JoinRequest request = createMemberRequest();
        request.setPassword("password");

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호, 비밀번호 재입력 불일치로 인한 예외 발생")
    void join_CustomNotValidException() throws Exception {
        JoinRequest request = createMemberRequest();
        request.setPassword("password123@");
        request.setRePassword("repassword123@");

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("회원 가입 시 이미 존재하는 아이디로 인해 예외 발생")
    void joinExistedUserId_dataExistException() throws Exception {
        JoinRequest request = createMemberRequest();
        memberRepository.save(createMember(null, request.getUserId()));

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_USERID.getMessage()));
    }


    @Test
    @DisplayName("회원가입 시 인증되지 않은 이메일로 예외 발생")
    void join_notVerifiedEmail() throws Exception {
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createMemberRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));;
    }



    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        String accessToken = jwtUtil.createToken(member.getUserId(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createLogoutDTO(member.getNickname()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 시 잘못된 access Token 으로 인해 예외 발생")
    void logout_customJwtBadRequestException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        String accessToken = jwtUtil.createToken(member.getUserId(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bea" + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLogoutDTO(member.getNickname()))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/logout : " + ErrorCode.INVALID_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 시 존재하지 않는 사용자 요청으로 인해 예외 발생")
    void logout_dataNotFoundException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        String accessToken = jwtUtil.createToken(member.getUserId(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLogoutDTO("notMember"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신")
    void refreshToken() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        String refreshToken = jwtUtil.createToken(member.getUserId(), 10000000);
        member.updateRefreshToken(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRefreshTokenRequest(member.getRefreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 refresh token 만료로 예외 발생")
    void refreshToken_unauthorizedExpiredException() throws Exception {
        String refreshToken = jwtUtil.createToken("member", -604800000);

        mockMvc.perform(post("/api/members/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRefreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 사용자 데이터 존재하지 않아 예외 발생")
    void refreshToken_memberNotFoundException() throws Exception {
        String refreshToken = jwtUtil.createToken("member", 100000000);

        mockMvc.perform(post("/api/members/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRefreshTokenRequest(refreshToken))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("토큰 갱신 시 사용자가 요청과 저장된 refresh token 값이 달라 예외 발생")
    void refreshToken_NotEqualsRefreshToken() throws Exception {
        Member member = memberRepository.save(createMember(null, "member"));
        String refreshToken = jwtUtil.createToken(member.getUserId(), 10000000);

        mockMvc.perform(post("/api/members/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRefreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("아이디 찾기")
    void findId() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));

        mockMvc.perform(post("/api/members/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindIdRequest(member.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("아이디 찾기 시 사용자 데이터 존재하지 않아 예외 발생")
    void findId_memberNotFoundException() throws Exception{
        mockMvc.perform(post("/api/members/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindIdRequest("notMember@email.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 찾기")
    void findPassword() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordDTO(member.getUserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 찾기 시 사용자 데이터 존재하지 않아 예외 발생")
    void findPassword_memberNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordDTO("notMember"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화")
    void resetPassword() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        when(redisUtil.getData(anyString())).thenReturn(member.getEmail());

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordDTO("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호와 재입력 비밀번호가 달라 예외 발생")
    void resetPassword_notMathPassword() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordDTO("changePassword", "password12!@", "password34!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 저장된 비밀번호 변경 토큰이 존재하지 않아 예외 발생")
    void changePassword_PasswordTokenNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordDTO("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CHANGE_PASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 사용자 데이터 존재하지 않아 예외 발생")
    void resetPassword_memberNotFoundException() throws Exception{
        when(redisUtil.getData(anyString())).thenReturn("noMember@email.com");

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordDTO("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경")
    void changePassword() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");

        memberRepository.save(createMember(null, "member", encodePassword));
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 입력값 조건 틀려 예외 발생")
    void changePassword_MethodArgumentNotValidException() throws Exception{
        memberRepository.save(createMember(null, "member"));
        ChangePasswordRequest request = createChangePasswordRequest("틀린값1", "test123!", "test123!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 변경 비밀번호와 재입력 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_inCorrectNewPassword() throws Exception{
        memberRepository.save(createMember(null, "member"));
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test456!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 현재 비밀번호와 변경 비밀번호가 같아 예외 발생")
    void changePassword_correctNowPassword() throws Exception{
        memberRepository.save(createMember(null, "member"));
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123@", "test123@");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD.getMessage()));
    }

    @Test
    @WithMockUser("member1")
    @DisplayName("사용자 정보를 찾을 수 없어 예외 발생")
    void changePassword_memberNotFoundException() throws Exception{
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 현재 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_incorrectSavedPassword() throws Exception{
        String encodePassword = passwordEncoder.encode("test123@");

        memberRepository.save(createMember(null, "member", encodePassword));
        ChangePasswordRequest request = createChangePasswordRequest("test123!", "test123!!", "test123!!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("사용자 정보 조회")
    void getMemberInfo() throws Exception{
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "profileImage"));
        Member member = memberRepository.save(createMember(null, "member", profileImage));

        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(member.getUserId()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("사용자 정보 조회 시 사용자 데이터 없어 예외 발생")
    void getMemberInfo_memberNotFoundException() throws Exception{
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @WithMockUser("member")
    @DisplayName("사용자 닉네임 변경")
    void changeNickname() throws Exception{
        memberRepository.save(createMember(null, "member"));
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @WithMockUser("member")
    @DisplayName("사용자 닉네임 변경 시 입력 조건이 맞지 않아 예외 발생")
    void changeNickname_methodInvalidArgumentException() throws Exception{
        memberRepository.save(createMember(null, "member"));
        ChangeNicknameRequest request = createChangeNicknameRequest("no");

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하의 영문 대/소문자, 한글, 숫자만 사용 가능합니다."));

    }

    @Test
    @WithMockUser("member1")
    @DisplayName("사용자 닉네임 변경 시 사용자 데이터 없어 예외 발생")
    void changeNickname_memberNotFoundException() throws Exception{
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("사용자 닉네임 변경 시 이미 존재하는 닉네임으로 예외 발생")
    void changeNickname_dataExistException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        ChangeNicknameRequest request = createChangeNicknameRequest(member.getNickname());

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage()));
    }


}
