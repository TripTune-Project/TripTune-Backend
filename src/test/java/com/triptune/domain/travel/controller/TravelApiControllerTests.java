package com.triptune.domain.travel.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@RequiredArgsConstructor
public class TravelApiControllerTests {

    private final WebApplicationContext wac;
    private final ObjectMapper objectMapper;

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
    @DisplayName("성공: 현재 위치에 따른 여행지 목록 제공")
    void findNearByTravelPlaceListSuccess() throws Exception {
        mockMvc.perform(post("/api/travel/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createTravelLocationRequest())))
                .andExpect(status().isOk());
    }

    private TravelLocationRequest createTravelLocationRequest(){
        return TravelLocationRequest.builder()
                .longitude(37.82312)
                .latitude(127.44233)
                .build();
    }

    private String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
