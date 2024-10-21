package com.triptune.domain.travel.controller;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.travel.TravelTest;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TravelApiControllerTests extends TravelTest {

    private final WebApplicationContext wac;
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final FileRepository fileRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;

    @Autowired
    public TravelApiControllerTests(WebApplicationContext wac, TravelPlacePlaceRepository travelPlaceRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, FileRepository fileRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository) {
        this.wac = wac;
        this.travelPlaceRepository = travelPlaceRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.fileRepository = fileRepository;
        this.travelImageRepository = travelImageRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
    }



    private MockMvc mockMvc;
    private TravelPlace travelPlace;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "강남"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        File file1 = fileRepository.save(createFile("test1", true));
        File file2 = fileRepository.save(createFile("test2", false));;

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, file2));
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);

        travelPlace.setTravelImageList(travelImageList);

    }

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 현재 위치에 따른 여행지 목록을 제공하며 조회 결과가 존재하는 경우")
    void getNearByTravelPlaces_withData() throws Exception {
        // given

        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(127.0281573537, 37.4970465429))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 현재 위치에 따른 여행지 목록을 제공하며 조회 결과가 존재하지 않는 경우")
    void getNearByTravelPlaces_noData() throws Exception {
        mockMvc.perform(post("/api/travels")
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
        // given
        ApiContentType apiContentType = createApiContentType("관광지");
        apiContentTypeRepository.save(apiContentType);
        travelPlace.setApiContentType(apiContentType);

        // when, then
        mockMvc.perform(get("/api/travels/{placeId}", travelPlace.getPlaceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeId").value(travelPlace.getPlaceId()))
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


}
