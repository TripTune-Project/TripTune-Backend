package com.triptune.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.member.dto.LogoutDTO;
import com.triptune.domain.member.dto.RefreshTokenRequest;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
@Transactional
public class MemberApiControllerTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private RedisUtil redisUtil;

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
    @DisplayName("logout() 성공: 로그아웃")
    void logout_success() throws Exception{
        String accessToken = jwtUtil.createToken("test", 3600);

        doNothing().when(memberRepository).deleteRefreshToken("test");
        doNothing().when(redisUtil).saveExpiredData(accessToken, "logout", 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createLogoutDTO())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("logout() 실패: 잘못된 access Token 으로 인해 CustomJwtBadRequestException 발생")
    void logout_customJwtBadRequestException_fail() throws Exception{
        String accessToken = jwtUtil.createToken("test", 3600);

        doNothing().when(memberRepository).deleteRefreshToken("test");
        doNothing().when(redisUtil).saveExpiredData(accessToken, "logout", 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bea" + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLogoutDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_JWT_TOKEN.getMessage()));
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


    private LogoutDTO createLogoutDTO(){
        return LogoutDTO.builder()
                .userId("test")
                .build();
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
