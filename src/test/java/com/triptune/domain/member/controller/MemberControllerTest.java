package com.triptune.domain.member.controller;

import com.triptune.domain.member.MemberTest;
import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.service.MemberService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.filter.JwtAuthFilter;
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

import static org.mockito.Mockito.doNothing;
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

    @MockBean
    private RedisUtil redisUtil;

    private MockMvc mockMvc;

    @Autowired
    public MemberControllerTest(WebApplicationContext wac, JwtUtil jwtUtil, MemberRepository memberRepository) {
        this.wac = wac;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new JwtAuthFilter(jwtUtil))
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("join(): 회원가입 성공")
    void join() throws Exception {
        mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createMemberRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));;
    }

    @Test
    @DisplayName("join(): 비밀번호 유효성 검사로 인해 예외 발생")
    void joinInvalidPassword_methodArgumentNotValidException() throws Exception {
        MemberRequest request = createMemberRequest();
        request.setPassword("password");

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("join(): 비밀번호, 비밀번호 재입력 불일치로 인한 예외 발생")
    void join_CustomNotValidException() throws Exception {
        MemberRequest request = createMemberRequest();
        request.setPassword("password123@");
        request.setRepassword("repassword123@");

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("join(): 이미 존재하는 아이디로 인해 예외 발생")
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
    @DisplayName("logout(): 로그아웃 성공")
    void logout() throws Exception{
        Member member = memberRepository.save(createMember(null, "member1"));
        String accessToken = jwtUtil.createToken(member.getUserId(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createLogoutDTO())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("logout(): 잘못된 access Token 으로 인해 예외 발생")
    void logout_customJwtBadRequestException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member1"));
        String accessToken = jwtUtil.createToken(member.getUserId(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bea" + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLogoutDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_JWT_TOKEN.getMessage()));
    }


    @Test
    @DisplayName("refreshToken(): refresh token 만료로 예외 발생")
    void refreshTokenExpired_unauthorizedException() throws Exception {
        String refreshToken = jwtUtil.createToken("test", -604800000);

        mockMvc.perform(post("/api/members/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRefreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(401));

    }

}
