package com.triptune.bookmark.controller;

import com.triptune.bookmark.BookmarkTest;
import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.repository.ApiCategoryRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.response.enums.SuccessCode;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("h2")
class BookmarkControllerTest extends BookmarkTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;

    private MockMvc mockMvc;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new JwtAuthFilter(jwtUtils))
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
    void createBookmark() throws Exception{
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));

        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(travelPlace.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(travelPlace.getBookmarkCnt()).isEqualTo(1);
    }

    @ParameterizedTest
    @DisplayName("북마크 생성 시 1보다 작은 값 입력으로 에러 발생")
    @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
    void createBookmark_invalidMinPlaceId(Long input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(input);

        // when,  then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행지 ID는 1 이상의 값이어야 합니다."));
    }

    @Test
    @DisplayName("북마크 생성 시 placeId 값이 null 로 에러 발생")
    void createBookmark_invalidNullPlaceId() throws Exception{
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(null);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행지 ID는 필수 입력 값입니다."));

    }


    @Test
    @DisplayName("북마크 추가 시 이미 북마크로 등록되어 있어 예외 발생")
    void createBookmark_alreadyBookmarked() throws Exception{
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        bookmarkRepository.save(createBookmark(null, member, travelPlace, LocalDateTime.now()));

        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(travelPlace.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_BOOKMARK.getMessage()));
    }


    @Test
    @DisplayName("북마크 추가 시 회원 데이터 없어 예외 발생")
    void createBookmark_memberNotFound() throws Exception{
        // given
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));

        Member member = createMember(0L, "notMember@email.com");
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(travelPlace.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 추가 시 여행지 데이터 없어 예외 발생")
    void createBookmark_placeNotFound() throws Exception{
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(1000L);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 삭제")
    void deleteBookmark() throws Exception{
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "여행지", 10));
        bookmarkRepository.save(createBookmark(null, member, travelPlace, LocalDateTime.now()));
        mockAuthentication(member);

        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", travelPlace.getPlaceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace.getPlaceId())).isFalse();
        assertThat(travelPlace.getBookmarkCnt()).isEqualTo(9);
    }


    @Test
    @DisplayName("북마크 삭제 시 북마크 데이터가 없는 경우")
    void deleteBookmark_bookmarkNotFound() throws Exception{
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "여행지", 0));
        mockAuthentication(member);

        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", travelPlace.getPlaceId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.BOOKMARK_NOT_FOUND.getMessage()));
    }


}