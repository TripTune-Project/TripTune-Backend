package com.triptune.email.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.email.EmailTest;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.dto.request.VerifyAuthRequest;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class EmailControllerTest extends EmailTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebApplicationContext wac;
    @Autowired private JwtUtils jwtUtils;


    @ParameterizedTest
    @DisplayName("이메일 인증 요청 시 값이 들어오지 않아 예외 발생")
    @ValueSource(strings = {"", " "})
    void verifyRequest_invalidNotBlank(String input) throws Exception{
        // given
        EmailRequest request = createEmailRequest(input);

        // when, then
        mockMvc.perform(post("/api/emails/verify-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }


    @Test
    @DisplayName("이메일 인증 요청 시 null 값이 들어와 예외 발생")
    void verifyRequest_invalidNull() throws Exception{
        // given
        EmailRequest request = createEmailRequest(null);

        // when, then
        mockMvc.perform(post("/api/emails/verify-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("이메일 인증 요청 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void verifyRequest_invalidEmail(String input) throws Exception{
        // given
        EmailRequest request = createEmailRequest(input);

        // when, then
        mockMvc.perform(post("/api/emails/verify-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));

    }


    @ParameterizedTest
    @DisplayName("이메일 인증 시 이메일에 값이 들어오지 않아 예외 발생")
    @ValueSource(strings = {"", " "})
    void verify_invalidNotBlankEmail(String input) throws Exception{
        // given
        VerifyAuthRequest request = createVerifyAuthRequest(input, "authCode");

        // when, then
        mockMvc.perform(post("/api/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }


    @Test
    @DisplayName("이메일 인증 시 이메일에 null 값이 들어와 예외 발생")
    void verify_invalidNullEmail() throws Exception{
        // given
        VerifyAuthRequest request = createVerifyAuthRequest(null, "authCode");

        // when, then
        mockMvc.perform(post("/api/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("이메일 인증 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void verify_invalidEmail(String input) throws Exception{
        // given
        VerifyAuthRequest request = createVerifyAuthRequest(input, "authCode");

        // when, then
        mockMvc.perform(post("/api/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));

    }


    @ParameterizedTest
    @DisplayName("이메일 인증 시 인증번호 값이 들어오지 않아 예외 발생")
    @ValueSource(strings = {"", " "})
    void verify_invalidNotBlankAuthCode(String input) throws Exception{
        // given
        VerifyAuthRequest request = createVerifyAuthRequest("member@email.com", input);

        // when, then
        mockMvc.perform(post("/api/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("인증번호는 필수 입력 값입니다.")));

    }


    @Test
    @DisplayName("이메일 인증 시 인증번호에 null 값이 들어와 예외 발생")
    void verify_invalidNullAuthCode() throws Exception{
        // given
        VerifyAuthRequest request = createVerifyAuthRequest("member@email.com", null);

        // when, then
        mockMvc.perform(post("/api/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("인증번호는 필수 입력 값입니다.")));

    }

}