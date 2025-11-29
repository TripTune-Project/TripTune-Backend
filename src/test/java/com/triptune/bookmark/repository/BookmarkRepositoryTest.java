package com.triptune.bookmark.repository;

import com.triptune.bookmark.BookmarkTest;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.repository.*;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
class BookmarkRepositoryTest extends BookmarkTest {

    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;


    private Member member;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        member = memberRepository.save(createMember(null, "member@email.com"));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "가장소", 0));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "가장소",5));
        travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "나장소",2));
    }

    @Test
    @DisplayName("북마크 생성 시 생성일 자동 입력 확인")
    void createBookmark() {
        // given
        Bookmark bookmark = Bookmark.from(member, travelPlace1);

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
        bookmarkRepository.save(createBookmark(null, member, travelPlace1));
        Thread.sleep(10);
        bookmarkRepository.save(createBookmark(null, member, travelPlace2));
        Thread.sleep(10);
        bookmarkRepository.save(createBookmark(null, member, travelPlace3));

        // when
        Page<TravelPlace> response = bookmarkRepository.findSortedMemberBookmarks(member.getMemberId(), pageable, BookmarkSortType.NEWEST);

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getContent().get(0)).isEqualTo(travelPlace3);
        assertThat(response.getContent().get(1)).isEqualTo(travelPlace2);
        assertThat(response.getContent().get(2)).isEqualTo(travelPlace1);
    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 - 이름순")
    void getBookmarkTravelPlaces_sortName(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);
        bookmarkRepository.save(createBookmark(null, member, travelPlace1));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3));

        // when
        Page<TravelPlace> response = bookmarkRepository.findSortedMemberBookmarks(member.getMemberId(), pageable, BookmarkSortType.NAME);

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getContent().get(0)).isEqualTo(travelPlace1);
        assertThat(response.getContent().get(1)).isEqualTo(travelPlace2);
        assertThat(response.getContent().get(2)).isEqualTo(travelPlace3);
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