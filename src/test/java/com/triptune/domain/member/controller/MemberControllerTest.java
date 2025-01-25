package com.triptune.domain.member.controller;

import com.triptune.domain.common.service.S3Service;
import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.MemberTest;
import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.repository.ProfileImageRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class MemberControllerTest extends MemberTest{
    private final WebApplicationContext wac;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;

    @MockBean
    private RedisUtil redisUtil;

    @MockBean
    private EmailService emailService;

    @MockBean
    private S3Service s3Service;

    private MockMvc mockMvc;

    @Autowired
    public MemberControllerTest(WebApplicationContext wac, JwtUtil jwtUtil, MemberRepository memberRepository, ProfileImageRepository profileImageRepository) {
        this.wac = wac;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.profileImageRepository = profileImageRepository;
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
        mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createMemberRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));;
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호 유효성 검사로 예외 발생")
    void joinInvalidPassword_methodArgumentNotValidException() throws Exception {
        MemberRequest request = createMemberRequest();
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
        MemberRequest request = createMemberRequest();
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
        MemberRequest request = createMemberRequest();
        memberRepository.save(createMember(null, request.getUserId()));

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_USERID.getMessage()));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_JWT_TOKEN.getMessage()));
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
                .andExpect(jsonPath("$.message").value(ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

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
        Member member = memberRepository.save(createMember(null, "member"));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordDTO("notMember"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경")
    void changePassword() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        when(redisUtil.getData(anyString())).thenReturn(member.getEmail());

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createChangePasswordDTO("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호와 재입력 비밀번호가 달라 예외 발생")
    void changePassword_notMathPassword() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createChangePasswordDTO("changePassword", "password12!@", "password34!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경 시 저장된 비밀번호 변경 토큰이 존재하지 않아 예외 발생")
    void changePassword_PasswordTokenNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createChangePasswordDTO("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CHANGE_PASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경 시 사용자 데이터 존재하지 않아 예외 발생")
    void changePassword_memberNotFoundException() throws Exception{
        when(redisUtil.getData(anyString())).thenReturn("noMember@email.com");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createChangePasswordDTO("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }


}
