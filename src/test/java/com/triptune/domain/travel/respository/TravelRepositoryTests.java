package com.triptune.domain.travel.respository;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.travel.TravelTest;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocation;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.config.QueryDSLConfig;
import com.triptune.global.util.PageableUtil;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TravelRepositoryTests extends TravelTest {
    private final TravelRepository travelRepository;
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
    public TravelRepositoryTests(TravelRepository travelRepository, FileRepository fileRepository, CityRepository cityRepository, CountryRepository countryRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository) {
        this.travelRepository = travelRepository;
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
        travelPlace1 = travelRepository.save(createTravelPlace(country, city, district1, apiCategory));
        travelPlace2 = travelRepository.save(createTravelPlace(country, city, district2, apiCategory));
        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test2", false));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, file2));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, file1));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, file2));


        List<TravelImage> imageList1 = new ArrayList<>();
        imageList1.add(travelImage1);
        imageList1.add(travelImage2);
        List<TravelImage> imageList2 = new ArrayList<>();
        imageList2.add(travelImage3);
        imageList2.add(travelImage4);

        travelPlace1.setApiContentType(apiContentType);
        travelPlace1.setTravelImageList(imageList1);
        travelPlace2.setApiContentType(apiContentType);
        travelPlace2.setTravelImageList(imageList2);
    }
    

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaceList_withData(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelLocationRequest travelLocationRequest = createTravelLocationRequest(37.497, 127.0);
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocation> response = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, radius);

        // then
        List<TravelLocation> content = response.getContent();


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
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelLocationRequest travelLocationRequest = createTravelLocationRequest(99.999999, 99.999999);
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocation> response = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, radius);

        // then
        assertEquals(response.getTotalElements(), 0);
    }

    @Test
    @DisplayName("searchTravelPlacesWithLocation() 성공: 키워드 이용해 검색하며 검색 결과에 데이터가 존재하는 경우")
    void searchTravelPlacesWithLocation_withData(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "강남");

        // when
        Page<TravelLocation> response = travelRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        List<TravelLocation> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertTrue(content.get(0).getDistrict().contains("강남"));
    }


    @Test
    @DisplayName("searchTravelPlacesWithLocation() 성공: 키워드 이용해 검색하며 검색결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_noData(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "ㅁㄴㅇㄹ");

        // when
        Page<TravelLocation> response = travelRepository.searchTravelPlacesWithLocation(pageable, request);

        // then
        assertEquals(response.getTotalElements(), 0);
    }


    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색결과가 존재하는 경우")
    void searchTravelPlaces_withData(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        // when
        Page<TravelPlace> response = travelRepository.searchTravelPlaces(pageable, "성북");

        // then
        List<TravelPlace> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertTrue(content.get(0).getDistrict().getDistrictName().contains("성북"));
    }

    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색결과가 존재하지 않는ㄴ 경우")
    void searchTravelPlaces_noData(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        // when
        Page<TravelPlace> response = travelRepository.searchTravelPlaces(pageable, "ㅁㄴㅇㄹ");

        // then
        assertEquals(response.getTotalElements(), 0);
    }

}
