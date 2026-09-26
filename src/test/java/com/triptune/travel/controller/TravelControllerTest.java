package com.triptune.travel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.ApiContentTypeFixture;
import com.triptune.common.fixture.CityFixture;
import com.triptune.common.fixture.CountryFixture;
import com.triptune.common.fixture.DistrictFixture;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceDetailResponse;
import com.triptune.travel.dto.response.PlaceDistanceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.dto.response.TravelImageResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.service.TravelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TravelControllerTest {

    private static final String BASE_URL = "http://test.com/";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private TravelService travelService;


    private Country country;
    private City city;
    private District gangnam;
    private District seongdong;
    private ApiContentType attractionContentType;
    private ApiContentType lodgingContentType;

    private Member member;


    @BeforeEach
    void setUp() {
        country = CountryFixture.createCountry();
        city = CityFixture.createSeoul(country);
        gangnam = DistrictFixture.createDistrict(city, "강남구");
        seongdong = DistrictFixture.createDistrict(city, "성동구");

        attractionContentType = ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS);
        lodgingContentType = ApiContentTypeFixture.createApiContentType(ThemeType.LODGING);

        ProfileImage profileImage = ProfileImageFixture.createProfileImage("member1Image");
        member = MemberFixture.createNativeTypeMemberWithId(1L, "member@email.com", profileImage);
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_member() throws Exception {
        // given
        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                37.49,
                127.0281573537
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        TravelPlace seongdongPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        seongdong,
                        attractionContentType,
                        "여행지2",
                        37.4920,
                        127.0250
        );

        SecurityTestUtils.mockAuthentication(member);

        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(37.4970465429, 127.0281573537);

        List<PlaceDistanceResponse> placeDistances = List.of(
                TravelPlaceFixture.createPlaceDistanceResponse(seongdongPlace, null, 1.0, false),
                TravelPlaceFixture.createPlaceDistanceResponse(gangnamPlace, gangnamThumbUrl, 5.0, true)
        );

        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                placeDistances,
                PageUtils.defaultPageable(1),
                placeDistances.size()
        );

        given(travelService.getNearByTravelPlaces(anyInt(), anyLong(), any(PlaceLocationRequest.class)))
                .willReturn(response);


        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(seongdongPlace.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(seongdongPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("위치를 기반으로 여행지 목록을 조회 시 위도에 null 값이 들어와 400 반환")
    void getNearByTravelPlaces_invalidNullLatitude() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);
        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(null, 127.0281573537);

        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("위도는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("위치를 기반으로 여행지 목록을 조회 시 경도에 null 값이 들어와 400 반환")
    void getNearByTravelPlaces_invalidNullLongitude() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);
        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(37.4970465429, null);

        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("경도는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_member_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);
        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(9999.9999, 9999.9999);

        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        given(travelService.getNearByTravelPlaces(anyInt(), anyLong(), any(PlaceLocationRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_nonMember() throws Exception {
        // given
        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                37.49,
                127.0281573537
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        TravelPlace seongdongPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                seongdong,
                attractionContentType,
                "여행지2",
                37.4920,
                127.0250
        );

        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(37.4970465429, 127.0281573537);

        List<PlaceDistanceResponse> placeDistances = List.of(
                TravelPlaceFixture.createPlaceDistanceResponse(seongdongPlace, null, 1.0, false),
                TravelPlaceFixture.createPlaceDistanceResponse(gangnamPlace, gangnamThumbUrl, 5.0, false)
        );

        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                placeDistances,
                PageUtils.defaultPageable(1),
                placeDistances.size()
        );

        given(travelService.getNearByTravelPlaces(anyInt(), isNull(), any(PlaceLocationRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(seongdongPlace.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(seongdongPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }


    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_nonMember_emptyResult() throws Exception {
        // given
        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(9999.9999, 9999.9999);

        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        given(travelService.getNearByTravelPlaces(anyInt(), isNull(), any(PlaceLocationRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithLocation_member() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);

        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                37.49,
                127.0281573537
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        List<PlaceDistanceResponse> placeDistances = List.of(
                TravelPlaceFixture.createPlaceDistanceResponse(gangnamPlace, gangnamThumbUrl, 5.0, true)
        );
        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                placeDistances,
                PageUtils.defaultPageable(1),
                placeDistances.size()
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        given(travelService.searchTravelPlacesWithLocation(anyInt(), anyLong(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(gangnamPlace.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_member_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);

        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ");

        given(travelService.searchTravelPlacesWithLocation(anyInt(), anyLong(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithLocation_nonMember() throws Exception {
        // given
        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                37.49,
                127.0281573537
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        List<PlaceDistanceResponse> placeDistances = List.of(
                TravelPlaceFixture.createPlaceDistanceResponse(gangnamPlace, gangnamThumbUrl, 5.0, false)
        );
        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                placeDistances,
                PageUtils.defaultPageable(1),
                placeDistances.size()
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        given(travelService.searchTravelPlacesWithLocation(anyInt(), isNull(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(gangnamPlace.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_nonMember_emptyResult() throws Exception {
        // given
        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ");

        given(travelService.searchTravelPlacesWithLocation(anyInt(), isNull(), any(PlaceSearchRequest.class)))
                .willReturn(response);
        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("회원의 위치 정보 없이 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_member() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);

        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                37.49,
                127.0281573537
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        List<PlaceDistanceResponse> placeDistances = List.of(
                TravelPlaceFixture.createPlaceDistanceResponse(gangnamPlace, gangnamThumbUrl, 5.0, true)
        );
        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                placeDistances,
                PageUtils.defaultPageable(1),
                placeDistances.size()
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("강남");

        given(travelService.searchTravelPlacesWithoutLocation(anyInt(), anyLong(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(gangnamPlace.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("회원의 위치 정보 없이 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_member_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);

        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("ㅁㄴㅇㄹ");

        given(travelService.searchTravelPlacesWithoutLocation(anyInt(), anyLong(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("비회원의 위치 정보 없이 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_nonMember() throws Exception {
        // given
        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                37.49,
                127.0281573537
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        List<PlaceDistanceResponse> placeDistances = List.of(
                TravelPlaceFixture.createPlaceDistanceResponse(gangnamPlace, gangnamThumbUrl, 5.0, false)
        );
        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                placeDistances,
                PageUtils.defaultPageable(1),
                placeDistances.size()
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("강남");

        given(travelService.searchTravelPlacesWithoutLocation(anyInt(), isNull(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(gangnamPlace.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("비회원의 위치 정보 없이 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_nonMember_emptyResult() throws Exception {
        // given
        Page<PlaceDistanceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("ㅁㄴㅇㄹ");

        given(travelService.searchTravelPlacesWithoutLocation(anyInt(), isNull(), any(PlaceSearchRequest.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @ParameterizedTest
    @DisplayName("여행지 검색 시 키워드에 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void searchTravelPlaces_invalidNotBlankKeyword(String input) throws Exception {
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(
                37.4970465429,
                127.0281573537,
                input
        );

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("검색어는 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("여행지 검색 시 키워드에 null 값으로 400 반환")
    void searchTravelPlaces_invalidNullKeyword() throws Exception {
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(
                37.4970465429,
                127.0281573537,
                null
        );

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("검색어는 필수 입력 값입니다."));
    }

    @ParameterizedTest
    @DisplayName("여행지 검색 시 키워드에 특수문자가 존재해 400 반환")
    @ValueSource(strings = {"@강남", "#", "SELECT * FROM MEMBER"})
    void searchTravelPlaces_invalidKeyword(String keyword) throws Exception {
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(
                37.4970465429,
                127.0281573537,
                keyword
        );

        // when, then
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("검색어에 특수문자는 사용 불가합니다."));
    }

    @Test
    @DisplayName("회원의 여행지 상세정보 조회")
    void getTravelDetails_member() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member);

        TravelPlace place = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1"
        );
        TravelImage image1 = TravelImageFixture.createTravelImage(place, "test1", true);
        TravelImage image2 = TravelImageFixture.createTravelImage(place, "test2", false);

        List<TravelImageResponse> travelImages = List.of(
                TravelImageFixture.createTravelImageResponse(image1, BASE_URL + image1.getS3ObjectKey()),
                TravelImageFixture.createTravelImageResponse(image2, BASE_URL + image2.getS3ObjectKey())
        );

        PlaceDetailResponse response = TravelPlaceFixture.createPlaceDetailResponse(place, travelImages, true);

        given(travelService.getTravelPlaceDetails(anyLong(), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/travels/{placeId}", place.getPlaceId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.placeName").value(place.getPlaceName()))
                .andExpect(jsonPath("$.data.placeName").exists())
                .andExpect(jsonPath("$.data.imageList").isNotEmpty())
                .andExpect(jsonPath("$.data.bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("비회원의 여행지 상세정보 조회")
    void getTravelDetails_nonMember() throws Exception {
        // given
        TravelPlace place = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1"
        );
        TravelImage image1 = TravelImageFixture.createTravelImage(place, "test1", true);
        TravelImage image2 = TravelImageFixture.createTravelImage(place, "test2", false);

        List<TravelImageResponse> travelImages = List.of(
                TravelImageFixture.createTravelImageResponse(image1, BASE_URL + image1.getS3ObjectKey()),
                TravelImageFixture.createTravelImageResponse(image2, BASE_URL + image2.getS3ObjectKey())
        );
        PlaceDetailResponse response = TravelPlaceFixture.createPlaceDetailResponse(
                place,
                travelImages,
                false
        );

        given(travelService.getTravelPlaceDetails(anyLong(), isNull()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/travels/{placeId}", place.getPlaceId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.placeName").value(place.getPlaceName()))
                .andExpect(jsonPath("$.data.placeName").exists())
                .andExpect(jsonPath("$.data.imageList").isNotEmpty())
                .andExpect(jsonPath("$.data.bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("여행지 상세정보 조회 시 데이터 존재하지 않아 404 반환")
    void getTravelDetails_placeNotFound() throws Exception {
        // given
        willThrow(new DataNotFoundException(ErrorCode.DATA_NOT_FOUND))
                .given(travelService).getTravelPlaceDetails(eq(1000L), isNull());

        // when, then
        mockMvc.perform(get("/api/travels/{placeId}", 1000L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DATA_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("지역별 인기 여행지 조회 - 전체")
    void findPopularTravelPlacesByCity_ALL() throws Exception {
        // given
        TravelPlace gangnamPlace = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                3
        );
        TravelImage gangnamThumb = TravelImageFixture.createTravelImage(gangnamPlace, "test1", true);
        String gangnamThumbUrl = BASE_URL + gangnamThumb.getS3ObjectKey();

        TravelPlace seongdongPlace = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                city,
                seongdong,
                attractionContentType,
                "여행지2",
                1

        );

        City busan = CityFixture.createBusan(country);
        District busanDistrict = DistrictFixture.createDistrict(busan, "금정구");
        TravelPlace busanPlace = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                busan,
                busanDistrict,
                attractionContentType,
                "금정 여행지",
                50
        );
        TravelImage busanThumb = TravelImageFixture.createTravelImage(busanPlace, "부산이미지1", true);
        String busanThumbUrl = BASE_URL + busanThumb.getS3ObjectKey();

        City jeolla = CityFixture.createCity(country, "전남광주통합특별시");
        District jeollaDistrict = DistrictFixture.createDistrict(busan, "보성구");
        TravelPlace jeollaPlace = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                jeolla,
                jeollaDistrict,
                attractionContentType,
                "보성 여행지",
                300
        );

        List<PlaceSimpleResponse> response = List.of(
                TravelPlaceFixture.createPlaceSimpleResponse(jeollaPlace, null),
                TravelPlaceFixture.createPlaceSimpleResponse(busanPlace, busanThumbUrl),
                TravelPlaceFixture.createPlaceSimpleResponse(gangnamPlace, gangnamThumbUrl),
                TravelPlaceFixture.createPlaceSimpleResponse(seongdongPlace, null)
        );

        given(travelService.getPopularTravelPlacesByCity(eq(CityType.ALL)))
                .willReturn(response);


        // when, then
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.ALL.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].placeName").value(jeollaPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data[1].placeName").value(busanPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").value(busanThumbUrl))
                .andExpect(jsonPath("$.data[2].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[2].thumbnailUrl").value(gangnamThumbUrl))
                .andExpect(jsonPath("$.data[3].placeName").value(seongdongPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[3].thumbnailUrl", nullValue()));

    }


    @Test
    @DisplayName("지역별 인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty() throws Exception {
        // given
        given(travelService.getPopularTravelPlacesByCity(eq(CityType.GYEONGSANG)))
                .willReturn(Collections.emptyList());

        // when, then
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.GYEONGSANG.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("지역별 인기 여행지 조회 시 파라미터 매칭 실패로 400 반환")
    void findPopularTravelPlacesByCity_illegalParam() throws Exception {
        // given, when, then
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", "seou"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_CITY_TYPE.getMessage()));
    }


    @Test
    @DisplayName("테마별 추천 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL() throws Exception{
        // given
        TravelPlace attractionPlace1 = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                city,
                gangnam,
                attractionContentType,
                "여행지1",
                1
        );
        TravelImage attraction1Thumb = TravelImageFixture.createTravelImage(attractionPlace1, "test1", true);
        String attraction1ThumbUrl = BASE_URL + attraction1Thumb.getS3ObjectKey();

        TravelPlace attractionPlace2 = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                city,
                seongdong,
                attractionContentType,
                "여행지2",
                2
        );

        City busan = CityFixture.createBusan(country);
        District busanDistrict = DistrictFixture.createDistrict(busan, "금정구");
        TravelPlace lodgingPlace = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                busan,
                busanDistrict,
                lodgingContentType,
                "부산 여행지"
        );
        TravelImage lodgingThumb = TravelImageFixture.createTravelImage(lodgingPlace, "부산이미지1", true);
        String lodgingThumbUrl = BASE_URL + lodgingThumb.getS3ObjectKey();

        City jeolla = CityFixture.createCity(country, "전라남도");
        District jeollaDistrict = DistrictFixture.createDistrict(busan, "보성구");
        TravelPlace attractionPlace3 = TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                country,
                jeolla,
                jeollaDistrict,
                attractionContentType,
                "전라도 여행지",
                3
        );

        List<PlaceSimpleResponse> response = List.of(
                TravelPlaceFixture.createPlaceSimpleResponse(attractionPlace3, null),
                TravelPlaceFixture.createPlaceSimpleResponse(attractionPlace2, null),
                TravelPlaceFixture.createPlaceSimpleResponse(attractionPlace1, attraction1ThumbUrl),
                TravelPlaceFixture.createPlaceSimpleResponse(lodgingPlace, lodgingThumbUrl)
        );

        given(travelService.getRecommendTravelPlacesByTheme(eq(ThemeType.ALL)))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.ALL.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].placeName").value(attractionPlace3.getPlaceName()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data[1].placeName").value(attractionPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data[2].placeName").value(attractionPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data[2].thumbnailUrl").value(attraction1ThumbUrl))
                .andExpect(jsonPath("$.data[3].placeName").value(lodgingPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[3].thumbnailUrl").value(lodgingThumbUrl));

    }

    @Test
    @DisplayName("테마별 추천 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty() throws Exception {
        // given
        given(travelService.getRecommendTravelPlacesByTheme(eq(ThemeType.FOOD)))
                .willReturn(Collections.emptyList());

        // when, then
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.FOOD.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    @DisplayName("테마별 추천 여행지 조회 시 파라미터 매칭 실패로 400 반환")
    void findRecommendTravelPlacesByTheme_illegalParam() throws Exception {
        // given, when, then
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", "foo"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_THEME_TYPE.getMessage()));

    }
}