package com.triptune.travel.respository;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.travel.TravelTest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
public class TravelPlaceRepositoryTest extends TravelTest {
    private final TravelPlaceRepository travelPlaceRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;

    private TravelPlace travelPlace1;

    @Autowired
    public TravelPlaceRepositoryTest(TravelPlaceRepository travelPlaceRepository, CityRepository cityRepository, CountryRepository countryRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository) {
        this.travelPlaceRepository = travelPlaceRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.travelImageRepository = travelImageRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
    }

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "성북구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        TravelPlace travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, "test2", false));

        travelPlace1.setApiContentType(apiContentType);
        travelPlace1.setTravelImageList(new ArrayList<>(List.of(travelImage1, travelImage2)));
        travelPlace2.setApiContentType(apiContentType);
        travelPlace2.setTravelImageList(new ArrayList<>(List.of(travelImage3, travelImage4)));
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


        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), travelPlace1.getCity().getCityName());
        assertEquals(content.get(0).getPlaceName(), travelPlace1.getPlaceName());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertNotEquals(content.get(0).getDistance(), 0.0);

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
        assertEquals(response.getTotalElements(), 0);
    }

    @Test
    @DisplayName("키워드 이용해 검색하며 검색 결과에 데이터가 존재하는 경우")
    void searchTravelPlacesWithLocation_withData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertTrue(content.get(0).getDistrict().contains("강남"));
    }


    @Test
    @DisplayName("키워드 이용해 검색하며 검색결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_noData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "ㅁㄴㅇㄹ");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        assertEquals(response.getTotalElements(), 0);
    }


    @Test
    @DisplayName("키워드 이용해 검색하며 검색결과가 존재하는 경우")
    void searchTravelPlaces_withData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<TravelPlace> response = travelPlaceRepository.searchTravelPlaces(pageable, "성북");

        // then
        List<TravelPlace> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertTrue(content.get(0).getDistrict().getDistrictName().contains("성북"));
    }

    @Test
    @DisplayName("키워드 이용해 검색하며 검색결과가 존재하지 않는 경우")
    void searchTravelPlaces_noData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        // when
        Page<TravelPlace> response = travelPlaceRepository.searchTravelPlaces(pageable, "ㅁㄴㅇㄹ");

        // then
        assertEquals(response.getTotalElements(), 0);
    }

}
