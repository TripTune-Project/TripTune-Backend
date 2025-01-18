package com.triptune.domain.mypage.controller;

import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.mypage.MyPageTest;
import com.triptune.domain.mypage.dto.request.MyPagePasswordRequest;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class MyPageControllerTest extends MyPageTest {

    private final WebApplicationContext wac;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @Autowired
    MyPageControllerTest(WebApplicationContext wac, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.wac = wac;
        this.memberRepository = memberRepository;
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
    @WithMockUser("member")
    @DisplayName("비밀번호 변경")
    void changePassword() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");

        memberRepository.save(createMember(0L, "member", encodePassword));
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123@", "test123!", "test123!");

        mockMvc.perform(patch("/api/mypage/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 입력값 조건 틀려 예외 발생")
    void changePassword_MethodArgumentNotValidException() throws Exception{
        memberRepository.save(createMember(0L, "member"));
        MyPagePasswordRequest request = createMyPagePasswordRequest("틀린값1", "test123!", "test123!");

        mockMvc.perform(patch("/api/mypage/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 변경 비밀번호와 재입력 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_inCorrectNewPassword() throws Exception{
        memberRepository.save(createMember(0L, "member"));
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123@", "test123!", "test456!");

        mockMvc.perform(patch("/api/mypage/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @WithMockUser("member")
    @DisplayName("비밀번호 변경 시 현재 비밀번호와 변경 비밀번호가 같아 예외 발생")
    void changePassword_correctNowPassword() throws Exception{
        memberRepository.save(createMember(0L, "member"));
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123@", "test123@", "test123@");

        mockMvc.perform(patch("/api/mypage/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD.getMessage()));
    }

    @Test
    @WithMockUser("member1")
    @DisplayName("사용자 정보를 찾을 수 없어 예외 발생")
    void changePassword_memberNotFoundException() throws Exception{
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123@", "test123!", "test123!");

        mockMvc.perform(patch("/api/mypage/account/change-password")
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

        memberRepository.save(createMember(0L, "member", encodePassword));
        MyPagePasswordRequest request = createMyPagePasswordRequest("test123!", "test123!!", "test123!!");

        mockMvc.perform(patch("/api/mypage/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }

}