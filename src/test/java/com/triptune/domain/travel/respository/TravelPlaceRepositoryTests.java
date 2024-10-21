package com.triptune.domain.travel.respository;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.travel.TravelTest;
import com.triptune.domain.travel.dto.PlaceLocationRequest;
import com.triptune.domain.travel.dto.PlaceLocation;
import com.triptune.domain.travel.dto.PlaceSearchRequest;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.config.QueryDSLConfig;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TravelPlaceRepositoryTests extends TravelTest {
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final FileRepository fileRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    @Autowired
    public TravelPlaceRepositoryTests(TravelPlacePlaceRepository travelPlaceRepository, FileRepository fileRepository, CityRepository cityRepository, CountryRepository countryRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository) {
        this.travelPlaceRepository = travelPlaceRepository;
        this.fileRepository = fileRepository;
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
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));
        File file1 = fileRepository.save(createFile("test1", true));
        File file2 = fileRepository.save(createFile("test2", false));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, file2));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, file1));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, file2));

        travelPlace1.setApiContentType(apiContentType);
        travelPlace1.setTravelImageList(Arrays.asList(travelImage1, travelImage2));
        travelPlace2.setApiContentType(apiContentType);
        travelPlace2.setTravelImageList(Arrays.asList(travelImage3, travelImage4));
    }
    

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaceList_withData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);
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
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록을 조회하며 조회 결과가 없는 경우")
    void findNearByTravelPlaceList_noData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);
        PlaceLocationRequest placeLocationRequest = createTravelLocationRequest(99.999999, 99.999999);
        int radius = 5;   // 5km 이내

        // when
        Page<PlaceLocation> response = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, radius);

        // then
        assertEquals(response.getTotalElements(), 0);
    }

    @Test
    @DisplayName("searchTravelPlacesWithLocation() 성공: 키워드 이용해 검색하며 검색 결과에 데이터가 존재하는 경우")
    void searchTravelPlacesWithLocation_withData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertTrue(content.get(0).getDistrict().contains("강남"));
    }


    @Test
    @DisplayName("searchTravelPlacesWithLocation() 성공: 키워드 이용해 검색하며 검색결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_noData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);
        PlaceSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "ㅁㄴㅇㄹ");

        // when
        Page<PlaceLocation> response = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        assertEquals(response.getTotalElements(), 0);
    }


    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색결과가 존재하는 경우")
    void searchTravelPlaces_withData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);

        // when
        Page<TravelPlace> response = travelPlaceRepository.searchTravelPlaces(pageable, "성북");

        // then
        List<TravelPlace> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertTrue(content.get(0).getDistrict().getDistrictName().contains("성북"));
    }

    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색결과가 존재하지 않는ㄴ 경우")
    void searchTravelPlaces_noData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);

        // when
        Page<TravelPlace> response = travelPlaceRepository.searchTravelPlaces(pageable, "ㅁㄴㅇㄹ");

        // then
        assertEquals(response.getTotalElements(), 0);
    }

}
