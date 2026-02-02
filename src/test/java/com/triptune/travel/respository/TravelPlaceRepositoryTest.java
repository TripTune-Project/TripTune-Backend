package com.triptune.travel.respository;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.global.util.PageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
public class TravelPlaceRepositoryTest {
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;

    private Country country;
    private City city;
    private District gangnam;
    private District junggu;
    private ApiCategory apiCategory;
    private ApiContentType attractionContentType;
    private ApiContentType sportsContentType;



    @BeforeEach
    void setUp(){
        country = countryRepository.save(CountryFixture.createCountry());
        city = cityRepository.save(CityFixture.createCity(country, "서울"));
        gangnam = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        junggu = districtRepository.save(DistrictFixture.createDistrict(city, "중구"));
        apiCategory = apiCategoryRepository.save(ApiCategoryFixture.createApiCategory());
        attractionContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS));
        sportsContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.SPORTS));
    }


    @Test
    @DisplayName("위치 정보에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaceList_withData(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        37.5,
                        127.0281573537
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        TravelPlace jungguPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        37.477,
                        127.0
                )
        );

        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceLocationRequest placeLocationRequest = TravelPlaceFixture.createTravelLocationRequest(37.497, 127.0);
        int radius = 5;   // 5km 이내

        // when
        Page<PlaceLocation> response = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, radius);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(jungguPlace.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(jungguPlace.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(jungguPlace.getAddress());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();
        assertThat(content.get(1).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(content.get(1).getDistance()).isNotNull();
        assertThat(content.get(1).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());
    }

    // TODO: 수정 후 다시 테스트(현재: 반경 5km 라서 데이터 안나옴, 수정 후: 반경에 상관없이 데이터 나와야함)
    @Test
    @DisplayName("위치 정보에 따른 여행지 목록을 조회하며 조회 결과가 없는 경우")
    void findNearByTravelPlaceList_emptyResult(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2"
                )
        );

        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceLocationRequest placeLocationRequest = TravelPlaceFixture.createTravelLocationRequest(99.999999, 99.999999);
        int radius = 5;   // 5km 이내

        // when
        Page<PlaceLocation> response = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, radius);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("키워드와 위치를 이용해 검색 시 검색 결과에 데이터가 존재하는 경우")
    void searchTravelPlacesWithLocation_withData(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
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
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        37.477,
                        127.0
                )
        );

        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(content.get(0).getDistrict()).isEqualTo(gangnamPlace.getDistrict().getDistrictName());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());
    }


    @Test
    @DisplayName("키워드와 위치를 이용해 검색 시 검색결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_emptyResult(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
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
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        37.477,
                        127.0
                )
        );
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.4970465429, 127.0281573537, "ㅁㄴㅇㄹ");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("기본 여행지(중구) 조회 시 결과가 존재하는 경우")
    void findDefaultTravelPlacesByJungGu_withData(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        37.49850,
                        127.02820
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        TravelPlace jungguPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        37.56420,
                        126.99800
                )
        );

        City busan = cityRepository.save(CityFixture.createCity(country, "부산"));
        District busanDistrict = districtRepository.save(DistrictFixture.createDistrict(busan, "금정구"));
        TravelPlace busanPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        busan,
                        busanDistrict,
                        apiCategory,
                        attractionContentType,
                        "부산 여행지",
                        35.15830,
                        129.06010
                )
        );
        TravelImage busanThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(busanPlace, "부산이미지1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(busanPlace, "부산이미지2", false));

        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<PlaceResponse> response = travelPlaceRepository.findDefaultTravelPlacesByJungGu(pageable);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getPlaceName()).isEqualTo(jungguPlace.getPlaceName());
        assertThat(content.get(0).getDistrict()).isEqualTo(jungguPlace.getDistrict().getDistrictName());
        assertThat(content.get(0).getThumbnailUrl()).isNull();
        assertThat(content.get(1).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(content.get(1).getDistrict()).isEqualTo(gangnamPlace.getDistrict().getDistrictName());
        assertThat(content.get(1).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());
        assertThat(content.get(2).getPlaceName()).isEqualTo(busanPlace.getPlaceName());
        assertThat(content.get(2).getDistrict()).isEqualTo(busanPlace.getDistrict().getDistrictName());
        assertThat(content.get(2).getThumbnailUrl()).isEqualTo(busanThumbnail.getS3ObjectUrl());

    }


    @Test
    @DisplayName("기본 여행지(중구) 조회 시 검색결과가 존재하지 않는 경우")
    void findDefaultTravelPlacesByJungGu_emptyResult(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<PlaceResponse> response = travelPlaceRepository.findDefaultTravelPlacesByJungGu(pageable);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("키워드 이용해 검색 시 검색결과가 존재하는 경우")
    void searchTravelPlaces_withData(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2"
                )
        );

        Pageable pageable = PageUtils.defaultPageable(1);

        String keyword = "강남";

        // when
        Page<PlaceResponse> response = travelPlaceRepository.searchTravelPlaces(pageable, keyword);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getDistrict()).contains(keyword);
        assertThat(content.get(0).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());
    }

    @Test
    @DisplayName("키워드 이용해 검색 시 검색결과가 존재하지 않는 경우")
    void searchTravelPlaces_emptyResult(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2"
                )
        );

        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<PlaceResponse> response = travelPlaceRepository.searchTravelPlaces(pageable, "ㅁㄴㅇㄹ");

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("인기 여행지 조회 - 전체")
    void findPopularTravelPlacesByCity_ALL(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        1
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        TravelPlace jungguPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        0
                )
        );

        City busan = cityRepository.save(CityFixture.createCity(country, "부산"));
        District busanDistrict = districtRepository.save(DistrictFixture.createDistrict(busan, "금정구"));
        TravelPlace busanPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        busan,
                        busanDistrict,
                        apiCategory,
                        attractionContentType,
                        "부산 여행지",
                        2
                )
        );
        TravelImage busanThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(busanPlace, "부산이미지1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(busanPlace, "부산이미지2", false));

        City jeolla = cityRepository.save(CityFixture.createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(DistrictFixture.createDistrict(busan, "보성구"));
        TravelPlace jeollaPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla,
                        jeollaDistrict,
                        apiCategory,
                        attractionContentType,
                        "전라 여행지",
                        3
                )
        );

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlaces(CityType.ALL);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(busanPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanThumbnail.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceName()).isEqualTo(jungguPlace.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("인기 여행지 조회 - 전라도")
    void findPopularTravelPlacesByCity_JEOLLA(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        1
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        0
                )
        );

        City jeolla1 = cityRepository.save(CityFixture.createCity(country, "전북특별자치도"));
        District jeolla1District = districtRepository.save(DistrictFixture.createDistrict(jeolla1, "고창군"));
        TravelPlace jeollaPlace1 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla1,
                        jeolla1District,
                        apiCategory, attractionContentType,
                        "고창 여행지",
                        1
                )
        );
        TravelImage jeolla1Thumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(jeollaPlace1, "부산이미지1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(jeollaPlace1, "부산이미지2", false));

        City jeolla2 = cityRepository.save(CityFixture.createCity(country, "전라남도"));
        District jeolla2District = districtRepository.save(DistrictFixture.createDistrict(jeolla2, "보성구"));
        TravelPlace jeollaPlace2 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla2,
                        jeolla2District,
                        apiCategory,
                        sportsContentType,
                        "보성 여행지",
                        2
                )
        );

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(jeollaPlace1.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(jeolla1Thumbnail.getS3ObjectUrl());

    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(TravelPlaceFixture.createTravelPlace(country, city, junggu, apiCategory, sportsContentType, "여행지2"));

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        1
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        TravelPlace jungguPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        0
                )
        );

        City busan = cityRepository.save(CityFixture.createCity(country, "부산"));
        District busanDistrict = districtRepository.save(DistrictFixture.createDistrict(busan, "금정구"));
        ApiContentType cultureContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.CULTURE));
        TravelPlace busanPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        busan,
                        busanDistrict,
                        apiCategory,
                        cultureContentType,
                        "여행지3",
                        2
                )
        );
        TravelImage busanImage1 = travelImageRepository.save(TravelImageFixture.createTravelImage(busanPlace, "부산이미지1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(busanPlace, "부산이미지2", false));

        City jeolla = cityRepository.save(CityFixture.createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(DistrictFixture.createDistrict(busan, "보성구"));
        TravelPlace jeollaPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla,
                        jeollaDistrict,
                        apiCategory,
                        attractionContentType,
                        "여행지4",
                        3
                )
        );

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlaces(ThemeType.All);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(busanPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanImage1.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceName()).isEqualTo(jungguPlace.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("추천 테마 여행지 조회 - 관광지")
    void findRecommendTravelPlacesByTheme_ATTRACTIONS(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1",
                        0
                )
        );
        TravelImage gangnamThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2",
                        1));

        City jeolla1 = cityRepository.save(CityFixture.createCity(country, "전북특별자치도"));
        District jeolla1District = districtRepository.save(DistrictFixture.createDistrict(jeolla1, "고창군"));
        ApiContentType cultureContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.CULTURE));
        TravelPlace jeollaPlace1 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla1,
                        jeolla1District,
                        apiCategory,
                        cultureContentType,
                        "여행지3",
                        2
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(jeollaPlace1, "부산이미지1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(jeollaPlace1, "부산이미지2", false));

        City jeolla2 = cityRepository.save(CityFixture.createCity(country, "전라남도"));
        District jeolla2District = districtRepository.save(DistrictFixture.createDistrict(jeolla2, "보성구"));
        TravelPlace jellaPlace2 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithBookmarkCnt(
                        country,
                        jeolla2,
                        jeolla2District,
                        apiCategory,
                        attractionContentType,
                        "여행지4",
                        3
                )
        );

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlaces(ThemeType.ATTRACTIONS);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jellaPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(gangnamPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(gangnamThumbnail.getS3ObjectUrl());

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty(){
        // given
        TravelPlace gangnamPlace = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        attractionContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(gangnamPlace, "test2", false));

        travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        junggu,
                        apiCategory,
                        sportsContentType,
                        "여행지2"
                )
        );

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlaces(ThemeType.FOOD);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }
}
