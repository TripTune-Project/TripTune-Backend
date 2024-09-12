package com.triptune.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.member.dto.RefreshTokenRequest;
import com.triptune.global.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class MemberApiControllerTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("refreshToken() 실패: refresh token 만료")
    void expiredRefreshToken_fail() throws Exception {
        String refreshToken = jwtUtil.createToken("test", -604800000);

        mockMvc.perform(post("/api/members/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTokenRefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(401));

    }


    private RefreshTokenRequest createTokenRefreshRequest(String refreshToken){
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    private String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
