package com.triptune.domain.travel.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.FileRepository;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocationResponse;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageFileRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Transactional
@Slf4j
public class TravelServiceTests {

    @InjectMocks
    private TravelService travelService;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TravelImageFileRepository travelImageFileRepository;

    private Country country;
    private City city;
    private District district;


    @BeforeEach
    void setUp(){
        this.country = Country.builder().countryId(1L).countryName("대한민국").build();
        this.city = City.builder().cityId(1L).cityName("서울").country(country).build();
        this.district = District.builder().districtId(1L).districtName("강남구").city(city).build();
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaces_withData_success(){
        // given
        TravelLocationRequest request = TravelLocationRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .build();

        int page = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelLocationResponse> mockTravelLocationResponse = new PageImpl<>(getTravelLocationResponses(pageable), pageable, 1);

        when(travelRepository.findNearByTravelPlaces(pageable, request, 5))
                .thenReturn(mockTravelLocationResponse);

        when(travelImageFileRepository.findByTravelPlacePlaceId(any()))
                .thenReturn(getTravelImageFiles());

        // when
        Page<TravelResponse> response = travelService.findNearByTravelPlaces(request, page);

        // then
        List<TravelResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), "서울");
        assertEquals(content.get(0).getPlaceName(), "테스트 장소명");
        assertEquals(content.get(0).getAddress(), "테스트 주소");
        assertEquals(content.get(0).getLatitude(), 37.5);
        assertEquals(content.get(0).getThumbnailUrl(), "/test/test1.jpg");
        assertNotEquals(content.get(0).getDistance(), 0.0);

        log.info("전체 갯수: {}", response.getTotalElements());
        log.info("전체 페이지수: {}", response.getTotalPages());
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void findNearByTravelPlaces_noData_success(){
        // given
        TravelLocationRequest request = TravelLocationRequest.builder()
                .latitude(0.0)
                .longitude(0.0)
                .build();

        int page = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelLocationResponse> mockTravelLocationResponse = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(travelRepository.findNearByTravelPlaces(pageable, request, 5))
                .thenReturn(mockTravelLocationResponse);

        // when
        Page<TravelResponse> response = travelService.findNearByTravelPlaces(request, page);

        // then
        assertEquals(response.getTotalElements(), 0);

        log.info("전체 갯수: {}", response.getTotalElements());
        log.info("전체 페이지수: {}", response.getTotalPages());
        log.info("내부 리스트 사이즈: {}", response.getContent().size());

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void searchTravelPlaces_withData_success(){
        // given
        TravelSearchRequest request = TravelSearchRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .keyword("테스트")
                .build();

        int page = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelLocationResponse> mockTravelLocationResponse = new PageImpl<>(getTravelLocationResponses(pageable), pageable, 1);

        when(travelRepository.searchTravelPlaces(pageable, request))
                .thenReturn(mockTravelLocationResponse);

        when(travelImageFileRepository.findByTravelPlacePlaceId(any()))
                .thenReturn(getTravelImageFiles());

        // when
        Page<TravelResponse> response = travelService.searchTravelPlaces(request, page);

        // then
        List<TravelResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), "서울");
        assertEquals(content.get(0).getPlaceName(), "테스트 장소명");
        assertEquals(content.get(0).getAddress(), "테스트 주소");
        assertEquals(content.get(0).getLatitude(), 37.5);
        assertEquals(content.get(0).getThumbnailUrl(), "/test/test1.jpg");
        assertNotEquals(content.get(0).getDistance(), 0.0);

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void searchTravelPlaces_noData_success(){
        // given
        TravelSearchRequest request = TravelSearchRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .keyword("ㅁㄴㅇㄹ")
                .build();

        int page = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelLocationResponse> mockTravelLocationResponse = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(travelRepository.searchTravelPlaces(pageable, request))
                .thenReturn(mockTravelLocationResponse);

        // when
        Page<TravelResponse> response = travelService.searchTravelPlaces(request, page);

        // then
        assertEquals(response.getTotalElements(), 0);

    }



    /**
     * 거리 정보가 들어간 List<TravelLocationResponse> 제공하는 메소드
     * @param pageable
     * @return List<TravelLocationResponse>
     */
    private List<TravelLocationResponse> getTravelLocationResponses(Pageable pageable){
        TravelLocationResponse travelLocationResponse = TravelLocationResponse.builder()
                .placeId(1L)
                .country(country.getCountryName())
                .city(city.getCityName())
                .district(district.getDistrictName())
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .distance(0.2345234234)
                .build();

        List<TravelLocationResponse> savedTravelLocationResponse = new ArrayList<>();
        savedTravelLocationResponse.add(travelLocationResponse);

        return savedTravelLocationResponse;
    }

    /**
     * 여행지 이미지 정보 List<TravelImageFile> 제공하는 메소드
     * @return List<TravelImageFile>
     */
    private List<TravelImageFile> getTravelImageFiles(){
        ApiCategory apiCategory = ApiCategory.builder().categoryCode("A0101").categoryName("자연").level(1).build();

        TravelPlace testTravel = TravelPlace.builder()
                .placeId(1L)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .contentTypeId(1L)
                .placeName("테스트 장소명")
                .address("테스트 주소")
                .latitude(37.5)
                .longitude(127.0281573537)
                .apiContentId(1)
                .createdAt(LocalDateTime.now())
                .build();

        File testFile1 = File.builder()
                .fileId(1L)
                .s3ObjectUrl("/test/test1.jpg")
                .originalName("test.jpg")
                .fileName("test1.jpg")
                .fileType("jpg")
                .fileSize(20)
                .createdAt(LocalDateTime.now())
                .isThumbnail(true)
                .build();

        File testFile2 = File.builder()
                .fileId(2L)
                .s3ObjectUrl("/test/test2.jpg")
                .originalName("test.jpg")
                .fileName("test2.jpg")
                .fileType("jpg")
                .fileSize(20)
                .createdAt(LocalDateTime.now())
                .isThumbnail(false)
                .build();


        return Arrays.asList(
                new TravelImageFile(1L, testTravel, testFile1),
                new TravelImageFile(2L, testTravel, testFile2)
        );
    }



}
