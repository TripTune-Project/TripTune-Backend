package com.triptune.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.BookmarkTest;
import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.response.enums.SuccessCode;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class BookmarkControllerTest extends BookmarkTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private TravelPlace place;

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country, "서울"));
        District district = districtRepository.save(createDistrict(city, "강남"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));
        place = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지"
                )
        );
    }

    @Test
    @DisplayName("북마크 추가")
    void createBookmark() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(place.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(place.getBookmarkCnt()).isEqualTo(1);
    }

    @ParameterizedTest
    @DisplayName("북마크 생성 시 1보다 작은 값 입력으로 에러 발생")
    @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
    void createBookmark_invalidMinPlaceId(Long input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(input);

        // when,  then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행지 ID는 1 이상의 값이어야 합니다."));
    }

    @Test
    @DisplayName("북마크 생성 시 placeId 값이 null 로 에러 발생")
    void createBookmark_invalidNullPlaceId() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(null);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행지 ID는 필수 입력 값입니다."));

    }


    @Test
    @DisplayName("북마크 추가 시 이미 북마크로 등록되어 있어 예외 발생")
    void createBookmark_alreadyBookmarked() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        bookmarkRepository.save(createBookmark(member, place));

        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(place.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_BOOKMARK.getMessage()));
    }


    @Test
    @DisplayName("북마크 추가 시 회원 데이터 없어 예외 발생")
    void createBookmark_memberNotFound() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);
        mockAuthentication(member);

        Long placeId = 1L;
        BookmarkRequest request = createBookmarkRequest(placeId);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 추가 시 여행지 데이터 없어 예외 발생")
    void createBookmark_placeNotFound() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        mockAuthentication(member);

        BookmarkRequest request = createBookmarkRequest(1000L);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 삭제")
    void deleteBookmark() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.save(createBookmark(member, place));
        place.increaseBookmarkCnt();

        mockAuthentication(member);

        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", place.getPlaceId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), place.getPlaceId())).isFalse();
        assertThat(place.getBookmarkCnt()).isEqualTo(0);
    }


    @Test
    @DisplayName("북마크 삭제 시 북마크 데이터가 없는 경우")
    void deleteBookmark_bookmarkNotFound() throws Exception{
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        mockAuthentication(member);

        Long placeId = 1L;

        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", placeId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.BOOKMARK_NOT_FOUND.getMessage()));
    }


}