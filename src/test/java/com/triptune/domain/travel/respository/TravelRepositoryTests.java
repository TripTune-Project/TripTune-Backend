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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
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

    private TravelPlace travelPlace;

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
        District district = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        travelPlace = travelRepository.save(createTravelPlace(country, city, district, apiCategory));
        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test1", false));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, file2));

        List<TravelImage> imageList = new ArrayList<>();
        imageList.add(travelImage1);
        imageList.add(travelImage2);

        travelPlace.setApiContentType(apiContentType);
        travelPlace.setTravelImageList(imageList);
    }
    

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaceList_withData_success(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelLocationRequest travelLocationRequest = createTravelLocationRequest(37.497, 127.0);
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocation> response = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, radius);

        // then
        List<TravelLocation> content = response.getContent();


        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), travelPlace.getCity().getCityName());
        assertEquals(content.get(0).getPlaceName(), travelPlace.getPlaceName());
        assertEquals(content.get(0).getAddress(), travelPlace.getAddress());
        assertNotEquals(content.get(0).getDistance(), 0.0);

    }

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록을 조회하며 조회 결과가 없는 경우")
    void findNearByTravelPlaceList_noData_success(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelLocationRequest travelLocationRequest = createTravelLocationRequest(99.999999, 99.999999);
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocation> result = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, radius);

        // then
        assertEquals(result.getTotalElements(), 0);
    }

    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색 결과에 데이터가 존재하는 경우")
    void searchTravelPlaces_withData_success(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "테스트");

        // when
        Page<TravelLocation> result = travelRepository.searchTravelPlaces(pageable, request);

        // then
        List<TravelLocation> content = result.getContent();
        assertTrue(content.get(0).getPlaceName().contains("테스트"));
        assertNotNull(content.get(0).getDistance());

        for(TravelLocation t: content){
            System.out.println("-------------------------------");
            System.out.println(t.getPlaceId());
            System.out.println(t.getDistance());
            System.out.println(t.getPlaceName());
        }
    }


    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색결과가 존재하지 않는 경우")
    void searchTravelPlaces_noData_success(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        TravelSearchRequest request = createTravelSearchRequest(37.4970465429, 127.0281573537, "ㅁㄴㅇㄹ");

        // when
        Page<TravelLocation> result = travelRepository.searchTravelPlaces(pageable, request);

        // then
        assertEquals(result.getTotalElements(), 0);
    }

}
