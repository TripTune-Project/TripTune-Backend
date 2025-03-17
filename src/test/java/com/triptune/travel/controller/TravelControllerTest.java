package com.triptune.travel.controller;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.TravelTest;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enumclass.CityType;
import com.triptune.travel.enumclass.ThemeType;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("h2")
public class TravelControllerTest extends TravelTest {
    @Autowired private WebApplicationContext wac;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;

    private MockMvc mockMvc;

    private Country country;
    private City city;
    private District district1;
    private ApiCategory apiCategory;
    private ApiContentType attractionContentType;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    private TravelImage travelImage1;
    private TravelImage travelImage2;

    private Member member;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        country = countryRepository.save(createCountry());
        city = cityRepository.save(createCity(country));
        district1 = districtRepository.save(createDistrict(city, "강남"));
        District district2 = districtRepository.save(createDistrict(city, "강남구"));
        apiCategory = apiCategoryRepository.save(createApiCategory());
        attractionContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory, attractionContentType, 0));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory, attractionContentType, 37.50303, 127.0731));

        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));

        member = memberRepository.save(createMember(null, "member"));
    }

    @Test
    @DisplayName("로그인한 사용자의 현재 위치에 따른 여행지 목록을 제공하며 데이터가 존재하는 경우")
    @WithMockUser("member")
    void getNearByTravelPlaces_loginAndExitsData() throws Exception {
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(37.4970465429, 127.0281573537))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("익명의 사용자의 현재 위치에 따른 여행지 목록을 제공하며 데이터가 존재하는 경우")
    void getNearByTravelPlaces_anonymousAndExistsData() throws Exception {
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(37.4970465429, 127.0281573537))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("로그인한 사용자의 현재 위치에 따른 여행지 목록을 제공하며 데이터가 존재하지 않는 경우")
    @WithMockUser("member")
    void getNearByTravelPlaces_loginAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(9999.9999, 9999.9999))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("익명의 사용자의 현재 위치에 따른 여행지 목록을 제공하며 데이터가 존재하지 않는 경우")
    void getNearByTravelPlaces_anonymousAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelLocationRequest(9999.9999, 9999.9999))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("로그인한 사용자의 여행지를 검색 시 검색 결과가 존재하는 경우 - 위치 데이터 존재")
    @WithMockUser("member")
    void searchTravelPlacesWithLocation_loginAndExistsData() throws Exception {
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(37.4970465429, 127.0281573537, "강남"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("익명 사용자의 여행지를 검색 시 검색 결과가 존재하는 경우 - 위치 데이터 존재")
    void searchTravelPlacesWithLocation_anonymousAndExistsData() throws Exception {
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(37.4970465429, 127.0281573537, "강남"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("로그인한 사용자의 여행지를 검색 시 검색 결과가 존재하지 않는 경우 - 위치 데이터 존재")
    @WithMockUser("member")
    void searchTravelPlacesWithLocation_loginAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("익명 사용자의 여행지를 검색 시 검색 결과가 존재하지 않는 경우 - 위치 데이터 존재")
    void searchTravelPlacesWithLocation_anonymousAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("로그인한 사용자의 여행지를 검색 시 검색 결과가 존재하는 경우 - 위치 데이터 존재하지 않는 경우")
    @WithMockUser("member")
    void searchTravelPlacesWithoutLocation_loginAndExistsData() throws Exception {
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest("강남"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("익명 사용자의 여행지를 검색 시 검색 결과가 존재하는 경우 - 위치 데이터 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_anonymousAndExistsData() throws Exception {
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest("강남"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("로그인한 사용자의 여행지를 검색 시 검색 결과가 존재하지 않는 경우 - 위치 데이터 존재하지 않는 경우")
    @WithMockUser("member")
    void searchTravelPlacesWithoutLocation_loginAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest("ㅁㄴㅇㄹ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("익명 사용자의 여행지를 검색 시 검색 결과가 존재하지 않는 경우 - 위치 데이터 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_anonymousAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest("ㅁㄴㅇㄹ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("여행지 검색 시 키워드에 특수문자가 존재하는 경우")
    void searchTravelPlaces_BadRequestException() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(37.4970465429, 127.0281573537, "@강남"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("검색어에 특수문자는 사용 불가합니다."));
    }

    @Test
    @DisplayName("로그인한 사용자의 여행지 상세정보 조회")
    @WithMockUser("member")
    void getTravelDetails_login() throws Exception {
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory, apiContentType, List.of(travelImage1, travelImage2)));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(get("/api/travels/{placeId}", travelPlace1.getPlaceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data.placeName").exists())
                .andExpect(jsonPath("$.data.imageList").isNotEmpty())
                .andExpect(jsonPath("$.data.bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("익명 사용자의 여행지 상세정보 조회")
    void getTravelDetails_anonymous() throws Exception {
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory, apiContentType, List.of(travelImage1, travelImage2)));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(get("/api/travels/{placeId}", travelPlace1.getPlaceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data.placeName").exists())
                .andExpect(jsonPath("$.data.imageList").isNotEmpty())
                .andExpect(jsonPath("$.data.bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("여행지 상세정보 조회 시 데이터 존재하지 않아 404 에러 발생")
    void getTravelDetails_NotFoundException() throws Exception {
        mockMvc.perform(get("/api/travels/{placeId}", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DATA_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("인기 여행지 조회 - 전체")
    void findPopularTravelPlacesByCity_ALL() throws Exception {
        City busan = cityRepository.save(createCity(country, "부산"));
        District busanDistrict = districtRepository.save(createDistrict(busan, "금정구"));
        TravelPlace travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, busan, busanDistrict, apiCategory, 5));
        TravelImage busanImage1 = travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지2", false));

        City jeolla = cityRepository.save(createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(createDistrict(busan, "보성구"));
        TravelPlace travelPlace4 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla, jeollaDistrict, apiCategory, 10));

        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.ALL.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data[0].placeId").value(travelPlace4.getPlaceId()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[1].placeId").value(travelPlace3.getPlaceId()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").value(busanImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[2].placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data[2].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[3].placeId").value(travelPlace2.getPlaceId()))
                .andExpect(jsonPath("$.data[3].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("인기 여행지 조회 - 경상도")
    void findPopularTravelPlacesByCity_GUEONGSANG() throws Exception {
        // given
        City gueongsang1 = cityRepository.save(createCity(country, "경상북도"));
        District gueongsang1District = districtRepository.save(createDistrict(gueongsang1, "구미시"));
        TravelPlace travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, gueongsang1, gueongsang1District, apiCategory, 10));
        TravelImage gueongsang1Image1 = travelImageRepository.save(createTravelImage(travelPlace3, "경상이미지1", true));
        travelImageRepository.save(createTravelImage(travelPlace3, "경상이미지2", false));

        City gueongsang2 = cityRepository.save(createCity(country, "경상남도"));
        District gueongsang2District = districtRepository.save(createDistrict(gueongsang2, "통영시"));
        TravelPlace travelPlace4 = travelPlaceRepository.save(createTravelPlace(null, country, gueongsang2, gueongsang2District, apiCategory, 5));

        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.GUEONGSANG.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data[0].placeId").value(travelPlace3.getPlaceId()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").value(gueongsang1Image1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[1].placeId").value(travelPlace4.getPlaceId()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").isEmpty());

    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty() throws Exception {
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.GUEONGSANG.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("인기 여행지 조회 시 파라미터 매칭 실패로 예외 발생")
    void findPopularTravelPlacesByCity_illegalParam() throws Exception {
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", "seou"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_CITY_TYPE.getMessage()));
    }


    @Test
    @DisplayName("추천 테마 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL() throws Exception{
        // given
        City busan = cityRepository.save(createCity(country, "부산"));
        District busanDistrict = districtRepository.save(createDistrict(busan, "금정구"));
        ApiContentType cultureContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.CULTURE));
        TravelPlace travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, busan, busanDistrict, apiCategory, cultureContentType, 5));
        TravelImage busanImage1 = travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지2", false));

        City jeolla = cityRepository.save(createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(createDistrict(busan, "보성구"));
        TravelPlace travelPlace4 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla, jeollaDistrict, apiCategory, attractionContentType, 10));

        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.All.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data[0].placeId").value(travelPlace4.getPlaceId()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[1].placeId").value(travelPlace3.getPlaceId()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").value(busanImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[2].placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data[2].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[3].placeId").value(travelPlace2.getPlaceId()))
                .andExpect(jsonPath("$.data[3].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("추천 테마 여행지 조회 - 관광지")
    void findRecommendTravelPlacesByTheme_ATTRACTIONS() throws Exception {
        // given
        City jeolla1 = cityRepository.save(createCity(country, "전북특별자치도"));
        District jeolla1District = districtRepository.save(createDistrict(jeolla1, "고창군"));
        ApiContentType cultureContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.CULTURE));
        TravelPlace travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla1, jeolla1District, apiCategory, cultureContentType, 5));
        travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지2", false));

        City jeolla2 = cityRepository.save(createCity(country, "전라남도"));
        District jeolla2District = districtRepository.save(createDistrict(jeolla2, "보성구"));
        TravelPlace travelPlace4 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla2, jeolla2District, apiCategory, attractionContentType, 10));

        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.ATTRACTIONS.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data[0].placeId").value(travelPlace4.getPlaceId()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[1].placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").value(travelImage1.getS3ObjectUrl()));
    }


    @Test
    @DisplayName("추천 테마 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty() throws Exception {
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.FOOD.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    @DisplayName("추천 테마 여행지 조회 시 파라미터 매칭 실패로 예외 발생")
    void findRecommendTravelPlacesByTheme_illegalParam() throws Exception {
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", "foo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_THEME_TYPE.getMessage()));

    }
}