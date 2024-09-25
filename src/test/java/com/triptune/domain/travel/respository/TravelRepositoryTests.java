package com.triptune.domain.travel.respository;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocationResponse;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.config.QueryDSLConfig;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TravelRepositoryTests {
    private final TravelRepository travelRepository;
    private final FileRepository fileRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;

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
        Country country = Country.builder().countryName("대한민국").build();
        Country savedCountry = countryRepository.save(country);

        City city = City.builder().cityName("서울").country(country).build();
        City savedCity = cityRepository.save(city);

        District district = District.builder().districtName("강남구").city(city).build();
        District savedDistrict = districtRepository.save(district);

        ApiCategory apiCategory = ApiCategory.builder().categoryCode("A0101").categoryName("자연").level(1).build();
        ApiCategory savedApiCategory = apiCategoryRepository.save(apiCategory);

        ApiContentType apiContentType = ApiContentType.builder().contentTypeId(1L).contentTypeName("관광지").build();
        ApiContentType savedApiContentType = apiContentTypeRepository.save(apiContentType);

        TravelPlace travelPlace = TravelPlace.builder()
                .country(savedCountry)
                .city(savedCity)
                .district(savedDistrict)
                .apiCategory(savedApiCategory)
                .apiContentType(savedApiContentType)
                .placeName("테스트 장소명")
                .address("테스트 주소")
                .useTime("상시")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .bookmarkCnt(0)
                .createdAt(LocalDateTime.now())
                .build();

        TravelPlace savedTravelPlace = travelRepository.save(travelPlace);

        File file = File.builder()
                .s3ObjectUrl("/test/test1.jpg")
                .originalName("test.jpg")
                .fileName("test1.jpg")
                .fileType("jpg")
                .fileSize(20)
                .createdAt(LocalDateTime.now())
                .isThumbnail(true)
                .build();

        File savedFile = fileRepository.save(file);

        TravelImage travelImageFile = TravelImage.builder()
                .travelPlace(savedTravelPlace)
                .file(savedFile)
                .build();

        travelImageRepository.save(travelImageFile);
    }
    

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaceList_withData_success(){
        // given
        Pageable pageable = PageRequest.of(0, 5);
        TravelLocationRequest travelLocationRequest = TravelLocationRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .build();
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocationResponse> response = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, radius);

        // then
        List<TravelLocationResponse> content = response.getContent();


        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), "서울");
        assertEquals(content.get(0).getPlaceName(), "테스트 장소명");
        assertEquals(content.get(0).getAddress(), "테스트 주소");
        assertEquals(content.get(0).getLatitude(), 37.5);
        assertNotEquals(content.get(0).getDistance(), 0.0);

    }

    @Test
    @DisplayName("findNearByTravelPlaceList() 성공: 위치 정보에 따른 여행지 목록을 조회하며 조회 결과가 없는 경우")
    void findNearByTravelPlaceList_noData_success(){
        // given
        Pageable pageable = PageRequest.of(0, 5);
        TravelLocationRequest travelLocationRequest = TravelLocationRequest.builder()
                .latitude(99.999999)
                .longitude(99.999999)
                .build();
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocationResponse> result = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, radius);

        // then
        assertEquals(result.getTotalElements(), 0);
    }

    @Test
    @DisplayName("searchTravelPlace() 성공: 키워드 이용해 검색하며 검색 결과에 데이터가 존재하는 경우")
    void searchTravelPlaces_withData_success(){
        // given
        Pageable pageable = PageRequest.of(0, 5);

        TravelSearchRequest request = TravelSearchRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .keyword("테스트")
                .build();

        // when
        Page<TravelLocationResponse> result = travelRepository.searchTravelPlaces(pageable, request);

        // then
        List<TravelLocationResponse> content = result.getContent();
        assertTrue(content.get(0).getPlaceName().contains("테스트"));
        assertNotNull(content.get(0).getDistance());

        for(TravelLocationResponse t: content){
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
        Pageable pageable = PageRequest.of(0, 5);

        TravelSearchRequest request = TravelSearchRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .keyword("ㅁㄴㅇㄹ")
                .build();

        // when
        Page<TravelLocationResponse> result = travelRepository.searchTravelPlaces(pageable, request);

        // then
        assertEquals(result.getTotalElements(), 0);
    }

}
