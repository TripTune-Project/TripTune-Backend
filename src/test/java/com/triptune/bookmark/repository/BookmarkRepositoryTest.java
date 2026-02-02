package com.triptune.bookmark.repository;

import com.triptune.bookmark.fixture.BookmarkFixture;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.global.util.PageUtils;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
class BookmarkRepositoryTest {

    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private ProfileImageRepository profileImageRepository;


    private Member member;
    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createCity(country, "서울"));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(ApiCategoryFixture.createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS));

        ProfileImage profileImage = profileImageRepository.save(ProfileImageFixture.createProfileImage("memberImage"));
        member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        place1 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지1"
                )
        );
        place2 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지2"
                )
        );
        place3 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지3"
                )
        );
    }

    @Test
    @DisplayName("북마크 생성 시 생성일 자동 입력 확인")
    void createBookmark() {
        // given
        Bookmark bookmark = Bookmark.createBookmark(member, place1);

        // when
        bookmarkRepository.save(bookmark);

        // then
        assertThat(bookmark.getCreatedAt()).isNotNull();

    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 - 최신순")
    void getBookmarkTravelPlaces_sortNewest() throws Exception{
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);
        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place1));
        Thread.sleep(10);
        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place2));
        Thread.sleep(10);
        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place3));

        // when
        Page<TravelPlace> response = bookmarkRepository.findSortedMemberBookmarks(
                member.getMemberId(),
                pageable,
                BookmarkSortType.NEWEST
        );

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getContent().get(0)).isEqualTo(place3);
        assertThat(response.getContent().get(1)).isEqualTo(place2);
        assertThat(response.getContent().get(2)).isEqualTo(place1);
    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 - 이름순")
    void getBookmarkTravelPlaces_sortName(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);
        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place1));
        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place2));
        bookmarkRepository.save(BookmarkFixture.createBookmark(member, place3));

        // when
        Page<TravelPlace> response = bookmarkRepository.findSortedMemberBookmarks(member.getMemberId(), pageable, BookmarkSortType.NAME);

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getContent().get(0)).isEqualTo(place1);
        assertThat(response.getContent().get(1)).isEqualTo(place2);
        assertThat(response.getContent().get(2)).isEqualTo(place3);
    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 시 데이터가 없는 경우")
    void getBookmarkTravelPlaces_emptyData(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);

        // when
        Page<TravelPlace> response = bookmarkRepository.findSortedMemberBookmarks(member.getMemberId(), pageable, BookmarkSortType.OLDEST);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }


}