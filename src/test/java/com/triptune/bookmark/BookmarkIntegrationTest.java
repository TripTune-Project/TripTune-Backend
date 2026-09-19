package com.triptune.bookmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.fixture.BookmarkFixture;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.ApiContentTypeFixture;
import com.triptune.common.fixture.CityFixture;
import com.triptune.common.fixture.CountryFixture;
import com.triptune.common.fixture.DistrictFixture;
import com.triptune.common.repository.ApiContentTypeRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import jakarta.persistence.EntityManager;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class BookmarkIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private EntityManager em;

    private TravelPlace place;
    private ProfileImage defaultImage;

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createSeoul(country));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS));
        place = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "여행지"
                )
        );

        defaultImage = profileImageRepository.save(ProfileImageFixture.createProfileImage("memberImage"));
    }

    @Test
    @DisplayName("북마크 추가")
    void createBookmark() throws Exception{
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", defaultImage));
        SecurityTestUtils.mockAuthentication(member);

        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(place.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        em.flush();
        em.clear();
        TravelPlace createdPlace = travelPlaceRepository.findById(place.getPlaceId()).orElseThrow();
        assertThat(createdPlace.getBookmarkCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 삭제")
    void deleteBookmark() throws Exception{
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", defaultImage));
        SecurityTestUtils.mockAuthentication(member);

        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place));
        place.increaseBookmarkCnt();

        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", place.getPlaceId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        em.flush();
        em.clear();

        assertThat(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), place.getPlaceId())).isFalse();
        TravelPlace deletedPlace = travelPlaceRepository.findById(place.getPlaceId()).orElseThrow();
        assertThat(deletedPlace.getBookmarkCnt()).isEqualTo(0);
    }

}