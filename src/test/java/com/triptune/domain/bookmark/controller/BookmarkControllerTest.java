package com.triptune.domain.bookmark.controller;

import com.triptune.domain.bookmark.BookmarkTest;
import com.triptune.domain.bookmark.repository.BookmarkRepository;
import com.triptune.domain.common.entity.ApiCategory;
import com.triptune.domain.common.entity.City;
import com.triptune.domain.common.entity.Country;
import com.triptune.domain.common.entity.District;
import com.triptune.domain.common.repository.ApiCategoryRepository;
import com.triptune.domain.common.repository.CityRepository;
import com.triptune.domain.common.repository.CountryRepository;
import com.triptune.domain.common.repository.DistrictRepository;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.filter.JwtAuthFilter;
import com.triptune.global.util.JwtUtil;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("h2")
class BookmarkControllerTest extends BookmarkTest {

    private final WebApplicationContext wac;
    private final JwtUtil jwtUtil;
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;

    private MockMvc mockMvc;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;

    @Autowired
    public BookmarkControllerTest(WebApplicationContext wac, JwtUtil jwtUtil, BookmarkRepository bookmarkRepository, MemberRepository memberRepository, TravelPlaceRepository travelPlaceRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository) {
        this.wac = wac;
        this.jwtUtil = jwtUtil;
        this.bookmarkRepository = bookmarkRepository;
        this.memberRepository = memberRepository;
        this.travelPlaceRepository = travelPlaceRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new JwtAuthFilter(jwtUtil))
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        country = countryRepository.save(createCountry());
        city = cityRepository.save(createCity(country));
        district = districtRepository.save(createDistrict(city, "강남"));
        apiCategory = apiCategoryRepository.save(createApiCategory());
    }

    @Test
    @DisplayName("북마크 추가")
    @WithMockUser("member")
    void createBookmark() throws Exception{
        memberRepository.save(createMember(null, "member"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));

        mockMvc.perform(post("/api/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createBookmarkRequest(travelPlace.getPlaceId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(travelPlace.getBookmarkCnt()).isEqualTo(1);
    }


    @Test
    @DisplayName("북마크 추가 시 이미 북마크로 등록되어 있어 예외 발생")
    @WithMockUser("member")
    void createBookmark_dataExistsException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        bookmarkRepository.save(createBookmark(null, member, travelPlace));

        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createBookmarkRequest(travelPlace.getPlaceId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_BOOKMARK.getMessage()));
    }


    @Test
    @DisplayName("북마크 추가 시 사용자 데이터 없어 예외 발생")
    @WithMockUser("member")
    void createBookmark_memberNotFoundException() throws Exception{
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));

        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createBookmarkRequest(travelPlace.getPlaceId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 추가 시 여행지 데이터 없어 예외 발생")
    @WithMockUser("member")
    void createBookmark_travelPlaceNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createBookmarkRequest(0L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 삭제")
    @WithMockUser("member")
    void deleteBookmark() throws Exception{
        Member member = memberRepository.save(createMember(null, "member"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        bookmarkRepository.save(createBookmark(null, member, travelPlace));

        mockMvc.perform(delete("/api/bookmarks/{placeId}", travelPlace.getPlaceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId(member.getUserId(), travelPlace.getPlaceId())).isFalse();
    }


    @Test
    @DisplayName("북마크 삭제 시 북마크 데이터가 없는 경우")
    @WithMockUser("member")
    void deleteBookmark_bookmarkNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));

        mockMvc.perform(delete("/api/bookmarks/{placeId}", travelPlace.getPlaceId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.BOOKMARK_NOT_FOUND.getMessage()));
    }






}