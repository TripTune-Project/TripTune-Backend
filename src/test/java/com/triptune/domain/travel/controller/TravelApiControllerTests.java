package com.triptune.domain.travel.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.global.enumclass.ErrorCode;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @DisplayName("findNearByTravelPlaceList() 성공: 현재 위치에 따른 여행지 목록을 제공하며 조회 결과가 존재하는 경우")
    void getNearByTravelPlaces_withData() throws Exception {
        mockMvc.perform(post("/api/travels/list")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(127.0281573537, 37.4970465429))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 현재 위치에 따른 여행지 목록을 제공하며 조회 결과가 존재하지 않는 경우")
    void getNearByTravelPlaces_noData() throws Exception {
        mockMvc.perform(post("/api/travels/list")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(9999.9999, 9999.9999))))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 키워드를 통해서 여행지를 검색하며 검색 결과가 존재하는 경우")
    void searchTravelPlaces_withData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(127.0281573537, 37.4970465429, "강남"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("searchTravelPlaces() 성공: 키워드를 통해서 여행지를 검색하며 검색 결과가 존재하지 않는 경우")
    void searchTravelPlaces_noData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("searchTravelPlaces() 실패: 키워드를 통해서 여행지를 검색하며 키워드에 특수문자가 존재하는 경우")
    void searchTravelPlaces_BadRequestException() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(127.0281573537, 37.4970465429, "@강남"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("검색어에 특수문자는 사용 불가합니다."));
    }

    @Test
    @DisplayName("getTravelDetails() 성공: 여행지 상세정보 조회")
    void getTravelDetails() throws Exception {
        mockMvc.perform(get("/api/travels/{placeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeId").value(1L))
                .andExpect(jsonPath("$.data.placeName").exists())
                .andExpect(jsonPath("$.data.imageList").isNotEmpty());
    }

    @Test
    @DisplayName("getTravelDetails() 실패: 여행지 상세정보 조회 시 데이터 존재하지 않아 404 에러 발생")
    void getTravelDetails_NotFoundException() throws Exception {
        mockMvc.perform(get("/api/travels/{placeId}", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DATA_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }


    /**
     * TravelLocationRequest 제공 메서드
     * @param longitude
     * @param latitude
     * @return TravelLocationRequest
     */
    private TravelLocationRequest createTravelLocationRequest(double longitude, double latitude){
        return TravelLocationRequest.builder()
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }

    /**
     * TravelSearchRequest 제공 메서드
     * @param longitude
     * @param latitude
     * @param keyword
     * @return TravelSearchRequest
     */
    private TravelSearchRequest createTravelSearchRequest(double longitude, double latitude, String keyword){
        return TravelSearchRequest.builder()
                .longitude(longitude)
                .latitude(latitude)
                .keyword(keyword)
                .build();
    }

    private String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
