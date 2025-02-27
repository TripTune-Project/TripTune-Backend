package com.triptune.travel.controller;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.TravelTest;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("h2")
public class TravelControllerTest extends TravelTest {

    private final WebApplicationContext wac;
    private final TravelPlaceRepository travelPlaceRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;


    private MockMvc mockMvc;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelImage travelImage1;
    private Member member;

    @Autowired
    public TravelControllerTest(WebApplicationContext wac, TravelPlaceRepository travelPlaceRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository, BookmarkRepository bookmarkRepository, MemberRepository memberRepository) {
        this.wac = wac;
        this.travelPlaceRepository = travelPlaceRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.travelImageRepository = travelImageRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.memberRepository = memberRepository;
    }


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남"));
        District district2 = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace1.setTravelImageList(travelImageList);

        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory, 37.50303, 127.0731));

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
    @DisplayName("로그인한 사용자의 여행지를 검색 시 검색 결과가 존재하는 경우")
    @WithMockUser("member")
    void searchTravelPlaces_loginAndExistsData() throws Exception {
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
    @DisplayName("익명 사용자의 여행지를 검색 시 검색 결과가 존재하는 경우")
    void searchTravelPlaces_anonymousAndExistsData() throws Exception {
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
    @DisplayName("로그인한 사용자의 여행지를 검색 시 검색 결과가 존재하지 않는 경우")
    @WithMockUser("member")
    void searchTravelPlaces_loginAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("익명 사용자의 여행지를 검색 시 검색 결과가 존재하지 않는 경우")
    void searchTravelPlaces_anonymousAndNoData() throws Exception {
        mockMvc.perform(post("/api/travels/search")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createTravelSearchRequest(9999.9999, 9999.9999, "ㅁㄴㅇㄹ"))))
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
        ApiContentType apiContentType = createApiContentType("관광지");
        apiContentTypeRepository.save(apiContentType);
        travelPlace1.setApiContentType(apiContentType);
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
        ApiContentType apiContentType = createApiContentType("관광지");
        apiContentTypeRepository.save(apiContentType);
        travelPlace1.setApiContentType(apiContentType);
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


}
