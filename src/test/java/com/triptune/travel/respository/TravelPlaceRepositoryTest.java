package com.triptune.travel.respository;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.travel.TravelTest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enumclass.CityType;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.config.QueryDSLConfig;
import com.triptune.global.util.PageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
public class TravelPlaceRepositoryTest extends TravelTest {
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelImageRepository travelImageRepository;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelImage travelImage1;

    private Country country;
    private City city;
    private ApiCategory apiCategory;

    @BeforeEach
    void setUp(){
        country = countryRepository.save(createCountry());
        city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "성북구"));
        apiCategory = apiCategoryRepository.save(createApiCategory());

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory, "강남구 여행지", 0));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory, "성북구 여행지", 0));

        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
    }
    

    @Test
    @DisplayName("위치 정보에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaceList_withData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceLocationRequest placeLocationRequest = createTravelLocationRequest(37.497, 127.0);
        int radius = 5;   // 5km 이내

        // when
        Page<PlaceLocation> response = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, radius);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(travelPlace1.getAddress());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(1).getDistance()).isNotNull();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("위치 정보에 따른 여행지 목록을 조회하며 조회 결과가 없는 경우")
    void findNearByTravelPlaceList_noData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceLocationRequest placeLocationRequest = createTravelLocationRequest(99.999999, 99.999999);
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
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getDistrict()).contains("강남");
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
    }


    @Test
    @DisplayName("키워드와 위치를 이용해 검색 시 검색결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_noData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "ㅁㄴㅇㄹ");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("지역을 이용해 여행지 조회 시 검색결과가 존재하는 경우")
    void findAllByAreaData_withData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<PlaceResponse> response = travelPlaceRepository.findAllByAreaData(pageable, country.getCountryName(), city.getCityName(), "성북구");

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getCountry()).isEqualTo(country.getCountryName());
        assertThat(content.get(0).getCity()).isEqualTo(city.getCityName());
        assertThat(content.get(0).getDistrict()).isEqualTo("성북구");
        assertThat(content.get(0).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("지역을 이용해 여행지 조회 시 검색결과가 존재하지 않는 경우")
    void findAllByAreaData_noData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<PlaceResponse> response = travelPlaceRepository.findAllByAreaData(pageable, "no", city.getCityName(), "성북");

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("키워드 이용해 검색 시 검색결과가 존재하는 경우")
    void searchTravelPlaces_withData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<PlaceResponse> response = travelPlaceRepository.searchTravelPlaces(pageable, "강남");

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getDistrict()).contains("강남");
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
    }

    @Test
    @DisplayName("키워드 이용해 검색 시 검색결과가 존재하지 않는 경우")
    void searchTravelPlaces_noData(){
        // given
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
        City busan = cityRepository.save(createCity(country, "부산"));
        District busanDistrict = districtRepository.save(createDistrict(busan, "금정구"));
        TravelPlace travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, busan, busanDistrict, apiCategory, "부산 여행지", 5));
        TravelImage busanImage1 = travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지2", false));

        City jeolla = cityRepository.save(createCity(country, "전라남도"));
        District jeollaDistrict = districtRepository.save(createDistrict(busan, "보성구"));
        TravelPlace travelPlace4 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla, jeollaDistrict, apiCategory, "전라 여행지", 10));

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlacesByCity(CityType.ALL);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(travelPlace4.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(travelPlace3.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanImage1.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceName()).isEqualTo(travelPlace2.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("인기 여행지 조회 - 전라도")
    void findPopularTravelPlacesByCity_JEOLLA(){
        // given
        City jeolla1 = cityRepository.save(createCity(country, "전북특별자치도"));
        District jeolla1District = districtRepository.save(createDistrict(jeolla1, "고창군"));
        TravelPlace travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla1, jeolla1District, apiCategory, "고창 여행지", 5));
        TravelImage jeolla1Image1 = travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지1", true));
        travelImageRepository.save(createTravelImage(travelPlace3, "부산이미지2", false));

        City jeolla2 = cityRepository.save(createCity(country, "전라남도"));
        District jeolla2District = districtRepository.save(createDistrict(jeolla2, "보성구"));
        TravelPlace travelPlace4 = travelPlaceRepository.save(createTravelPlace(null, country, jeolla2, jeolla2District, apiCategory, "보성 여행지", 10));

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlacesByCity(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(travelPlace4.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(travelPlace3.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(jeolla1Image1.getS3ObjectUrl());

    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty(){
        // given
        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlacesByCity(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }
}
