package com.triptune.travel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.travel.TravelTest;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class TravelControllerTest extends TravelTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private Country country;
    private City city;
    private District gangnam;
    private District seongdong;
    private ApiCategory apiCategory;
    private ApiContentType attractionContentType;
    private ApiContentType lodgingContentType;

    private Member member;


    @BeforeEach
    void setUp() {
        country = countryRepository.save(createCountry());
        city = cityRepository.save(createCity(country, "서울"));
        gangnam = districtRepository.save(createDistrict(city, "강남구"));
        seongdong = districtRepository.save(createDistrict(city, "성동구"));

        apiCategory = apiCategoryRepository.save(createApiCategory());
        attractionContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));
        lodgingContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.LODGING));

        ProfileImage profileImage = profileImageRepository.save(createProfileImage("member1Image"));
        member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_member() throws Exception {
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        37.49,
                        127.0281573537
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        TravelPlace seongdongPlace = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2",
                        37.4920,
                        127.0250
                )
        );

        bookmarkRepository.save(createBookmark(member, gangnamPlace));
        mockAuthentication(member);

        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("위치를 기반으로 여행지 목록을 조회 시 위도에 null 값이 들어와 예외 발생")
    void getNearByTravelPlaces_invalidNullLatitude() throws Exception {
        // given
        mockAuthentication(member);
        PlaceLocationRequest request = createTravelLocationRequest(null, 127.0281573537);

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
    @DisplayName("위치를 기반으로 여행지 목록을 조회 시 경도에 null 값이 들어와 예외 발생")
    void getNearByTravelPlaces_invalidNullLongitude() throws Exception {
        // given
        mockAuthentication(member);
        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, null);

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
        mockAuthentication(member);
        PlaceLocationRequest request = createTravelLocationRequest(9999.9999, 9999.9999);

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
        // 북마크 존재하지만 비회원이기 때문에 조회 결과 bookmarkStatus 는 false 이여야 함
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        37.5250,
                        127.0550
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        TravelPlace seongdongPlace = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2",
                        37.4700,
                        127.0000
                )
        );
        bookmarkRepository.save(createBookmark(member, gangnamPlace));

        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false))
                .andExpect(jsonPath("$.data.content[1].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].bookmarkStatus").value(false));
    }


    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_nonMember_emptyResult() throws Exception {
        // given
        PlaceLocationRequest request = createTravelLocationRequest(9999.9999, 9999.9999);

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
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        37.5250,
                        127.0550
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2",
                        37.4700,
                        127.0000
                )
        );
        bookmarkRepository.save(createBookmark(member, gangnamPlace));
        mockAuthentication(member);

        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_member_emptyResult() throws Exception {
        // given
        mockAuthentication(member);
        PlaceSearchRequest request = createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ");

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
        // 북마크 존재하지만 비회원이기 때문에 조회 결과 bookmarkStatus 는 false 이여야 함
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        37.5250,
                        127.0550
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2",
                        37.4700,
                        127.0000
                )
        );
        bookmarkRepository.save(createBookmark(member, gangnamPlace));
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_nonMember_emptyResult() throws Exception {
        // given
        PlaceSearchRequest request = createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ");

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
    @DisplayName("회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_member() throws Exception {
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2"
                    )
        );
        bookmarkRepository.save(createBookmark(member, gangnamPlace));
        mockAuthentication(member);

        PlaceSearchRequest request = createTravelSearchRequest("강남");

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(true));
    }

    @Test
    @DisplayName("회원의 위치를 기반하지 않고 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_member_emptyResult() throws Exception {
        // given
        mockAuthentication(member);
        PlaceSearchRequest request = createTravelSearchRequest("ㅁㄴㅇㄹ");

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
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_nonMember() throws Exception {
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2"
                )
        );
        bookmarkRepository.save(createBookmark(member, gangnamPlace));
        PlaceSearchRequest request = createTravelSearchRequest("강남");

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[0].bookmarkStatus").value(false));
    }

    @Test
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_nonMember_emptyResult() throws Exception {
        // given
        PlaceSearchRequest request = createTravelSearchRequest("ㅁㄴㅇㄹ");

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
    @DisplayName("여행지 검색 시 키워드에 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void searchTravelPlaces_invalidNotBlankKeyword(String input) throws Exception {
        // given
        PlaceSearchRequest request = createTravelSearchRequest(
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
    @DisplayName("여행지 검색 시 키워드에 null 값으로 예외 발생")
    void searchTravelPlaces_invalidNullKeyword() throws Exception {
        // given
        PlaceSearchRequest request = createTravelSearchRequest(
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
    @DisplayName("여행지 검색 시 키워드에 특수문자가 존재해 예외 발생")
    @ValueSource(strings = {"@강남", "#", "SELECT * FROM MEMBER"})
    void searchTravelPlaces_invalidKeyword(String keyword) throws Exception {
        // given
        PlaceSearchRequest request = createTravelSearchRequest(
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
        TravelPlace place = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(createTravelImage(place, "test1", true));
        travelImageRepository.save(createTravelImage(place, "test2", false));

        bookmarkRepository.save(createBookmark(member, place));
        mockAuthentication(member);

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
        TravelPlace place = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(createTravelImage(place, "test1", true));
        travelImageRepository.save(createTravelImage(place, "test2", false));

        bookmarkRepository.save(createBookmark(member, place));

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
    @DisplayName("여행지 상세정보 조회 시 데이터 존재하지 않아 예외 발생")
    void getTravelDetails_placeNotFound() throws Exception {
        // given, when, then
        mockMvc.perform(get("/api/travels/{placeId}", 0L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DATA_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("인기 여행지 조회 - 전체")
    void findPopularTravelPlacesByCity_ALL() throws Exception {
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        3
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));


        TravelPlace seongdongPlace = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2",
                        1
                )
        );

        City busan = cityRepository.save(createCity(country, "부산"));
        District busanDistrict = districtRepository.save(createDistrict(busan, "금정구"));
        TravelPlace busanPlace = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        busan,
                        busanDistrict,
                        apiCategory,
                        attractionContentType,
                        "금정 여행지",
                        50
                )
        );
        TravelImage busanThumbnail = travelImageRepository.save(createTravelImage(busanPlace, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(busanPlace, "부산이미지2", false));

        City jeolla = cityRepository.save(createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(createDistrict(busan, "보성구"));
        TravelPlace jeollaPlace = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla,
                        jeollaDistrict,
                        apiCategory,
                        attractionContentType,
                        "보성 여행지",
                        300
                )
        );

        // when, then
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.ALL.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].placeName").value(jeollaPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[1].placeName").value(busanPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").value(busanThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[2].placeName").value(gangnamPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[2].thumbnailUrl").value(gangnamThumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[3].placeName").value(seongdongPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[3].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("인기 여행지 조회 - 경상도")
    void findPopularTravelPlacesByCity_GUEONGSANG() throws Exception {
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2"
                )
        );

        City gueongsang1 = cityRepository.save(createCity(country, "경상북도"));
        District gueongsang1District = districtRepository.save(createDistrict(gueongsang1, "구미시"));
        TravelPlace gueongsangPlace1 = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        gueongsang1,
                        gueongsang1District,
                        apiCategory,
                        attractionContentType,
                        "구미 여행지"
                )
        );
        TravelImage gueongsang1Thumbnail = travelImageRepository.save(createTravelImage(gueongsangPlace1, "경상이미지1", true));
        travelImageRepository.save(createTravelImage(gueongsangPlace1, "경상이미지2", false));

        City gueongsang2 = cityRepository.save(createCity(country, "경상남도"));
        District gueongsang2District = districtRepository.save(createDistrict(gueongsang2, "통영시"));
        TravelPlace gueongsangPlace2 = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        gueongsang2,
                        gueongsang2District,
                        apiCategory,
                        attractionContentType,
                        "통영 여행지"
                )
        );

        // when, then
        mockMvc.perform(get("/api/travels/popular")
                        .param("city", CityType.GYEONGSANG.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].placeName").value(gueongsangPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[1].placeName").value(gueongsangPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").value(gueongsang1Thumbnail.getS3ObjectUrl()));

    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty() throws Exception {
        // given, when, then
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
    @DisplayName("인기 여행지 조회 시 파라미터 매칭 실패로 예외 발생")
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
    @DisplayName("추천 테마 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL() throws Exception{
        // given
        TravelPlace attractionPlace1 = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        1
                )
        );
        TravelImage attraction1Thumbnail = travelImageRepository.save(createTravelImage(attractionPlace1, "test1", true));
        travelImageRepository.save(createTravelImage(attractionPlace1, "test2", false));

        TravelPlace attractionPlace2 = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2",
                        2
                )
        );

        City busan = cityRepository.save(createCity(country, "부산"));
        District busanDistrict = districtRepository.save(createDistrict(busan, "금정구"));
        TravelPlace lodgingPlace = travelPlaceRepository.save(
                createLodgingTravelPlace(
                        country,
                        busan,
                        busanDistrict,
                        apiCategory,
                        lodgingContentType,
                        "부산 여행지"
                )
        );
        TravelImage lodgingThumbnail = travelImageRepository.save(createTravelImage(lodgingPlace, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(lodgingPlace, "부산이미지2", false));

        City jeolla = cityRepository.save(createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(createDistrict(busan, "보성구"));
        TravelPlace attractionPlace3 = travelPlaceRepository.save(
                createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla,
                        jeollaDistrict,
                        apiCategory,
                        attractionContentType,
                        "전라도 여행지",
                        3
                )
        );

        // when, then
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.All.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].placeName").value(attractionPlace3.getPlaceName()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[1].placeName").value(attractionPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data[1].thumbnailUrl").isEmpty())
                .andExpect(jsonPath("$.data[2].placeName").value(attractionPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data[2].thumbnailUrl").value(attraction1Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[3].placeName").value(lodgingPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[3].thumbnailUrl").value(lodgingThumbnail.getS3ObjectUrl()));

    }

    @Test
    @DisplayName("추천 테마 여행지 조회 - 숙박")
    void findRecommendTravelPlacesByTheme_Lodging() throws Exception {
        // given
        TravelPlace attractionPlace1 = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(createTravelImage(attractionPlace1, "test1", true));
        travelImageRepository.save(createTravelImage(attractionPlace1, "test2", false));

        travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        seongdong,
                        apiCategory,
                        attractionContentType,
                        "여행지2"
                )
        );

        City busan = cityRepository.save(createCity(country, "부산"));
        District busanDistrict = districtRepository.save(createDistrict(busan, "금정구"));
        TravelPlace lodgingPlace = travelPlaceRepository.save(
                createLodgingTravelPlace(
                        country,
                        busan,
                        busanDistrict,
                        apiCategory,
                        lodgingContentType,
                        "부산 여행지"
                )
        );
        TravelImage lodgingThumbnail = travelImageRepository.save(createTravelImage(lodgingPlace, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(lodgingPlace, "부산이미지2", false));


        // when, then
        mockMvc.perform(get("/api/travels/recommend")
                        .param("theme", ThemeType.LODGING.getValue()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].placeName").value(lodgingPlace.getPlaceName()))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").value(lodgingThumbnail.getS3ObjectUrl()));
    }


    @Test
    @DisplayName("추천 테마 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty() throws Exception {
        // given, when, then
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
    @DisplayName("추천 테마 여행지 조회 시 파라미터 매칭 실패로 예외 발생")
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