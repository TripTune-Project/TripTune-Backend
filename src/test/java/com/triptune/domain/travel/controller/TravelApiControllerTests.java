package com.triptune.domain.travel.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class TravelApiControllerTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("findNearByTravelPlaceList() 성공: 현재 위치에 따른 여행지 목록을 제공한다.")
    void findNearByTravelPlaces_success() throws Exception {
        mockMvc.perform(post("/api/travels/list")
                        .param("page", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createTravelLocationRequest())))
                .andExpect(status().isOk());
    }


//    @Test
//    @DisplayName("searchTravelPlaces() 성공: 키워드를 통해서 여행지를 겁색한다.")
//    void searchTravelPlaces_withData_success() throws Exception {
//        mockMvc.perform(get("/api/travels/search")
//                .param("page", "1")
//                .param("keyword", "강남"))
//                .andExpect(status().isOk());
//    }



    private TravelLocationRequest createTravelLocationRequest(){
        return TravelLocationRequest.builder()
                .longitude(127.0281573537)
                .latitude(37.4970465429)
                .build();
    }

    private String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
