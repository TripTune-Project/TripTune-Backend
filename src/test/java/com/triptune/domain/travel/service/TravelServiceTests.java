package com.triptune.domain.travel.service;

import com.triptune.domain.common.entity.*;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.domain.common.repository.FileRepository;
import com.triptune.domain.travel.dto.*;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.enumclass.ErrorCode;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
    private TravelImageRepository travelImageRepository;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;


    @BeforeEach
    void setUp(){
        this.country = Country.builder().countryId(1L).countryName("대한민국").build();
        this.city = City.builder().cityId(1L).cityName("서울").country(country).build();
        this.district = District.builder().districtId(1L).districtName("강남구").city(city).build();
        this.apiCategory = ApiCategory.builder().categoryCode("A0101").categoryName("자연").level(1).build();
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void getNearByTravelPlaces_withData(){
        // given
        TravelLocationRequest request = TravelLocationRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .build();

        int page = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelLocationResponse> mockTravelLocationResponse = new PageImpl<>(createTravelLocationResponses(), pageable, 1);

        when(travelRepository.findNearByTravelPlaces(pageable, request, 5))
                .thenReturn(mockTravelLocationResponse);

        when(travelImageRepository.findByTravelPlacePlaceId(any()))
                .thenReturn(createTravelImages());

        // when
        Page<TravelResponse> response = travelService.getNearByTravelPlaces(request, page);

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
    void getNearByTravelPlaces_noData(){
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
        Page<TravelResponse> response = travelService.getNearByTravelPlaces(request, page);

        // then
        assertEquals(response.getTotalElements(), 0);

        log.info("전체 갯수: {}", response.getTotalElements());
        log.info("전체 페이지수: {}", response.getTotalPages());
        log.info("내부 리스트 사이즈: {}", response.getContent().size());

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void searchTravelPlaces_withData(){
        // given
        TravelSearchRequest request = TravelSearchRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .keyword("테스트")
                .build();

        int page = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelLocationResponse> mockTravelLocationResponse = new PageImpl<>(createTravelLocationResponses(), pageable, 1);

        when(travelRepository.searchTravelPlaces(pageable, request))
                .thenReturn(mockTravelLocationResponse);

        when(travelImageRepository.findByTravelPlacePlaceId(any()))
                .thenReturn(createTravelImages());

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
    void searchTravelPlaces_noData(){
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
    
    @Test
    @DisplayName("getTravelDetails() 성공: 숙박을 제외한 여행지 상세조회")
    void getTravelDetails_exceptLodging(){
        // given
        ApiContentType apiContentType = ApiContentType.builder().contentTypeId(1L).contentTypeName("관광지").build();
        TravelPlace travelPlace = createTravelPlace();
        travelPlace.setUseTime("상시");
        travelPlace.setApiContentType(apiContentType);

        when(travelRepository.findByPlaceId(1L))
                .thenReturn(Optional.of(travelPlace));

        // when
        TravelDetailResponse response = travelService.getTravelPlaceDetails(1L);

        // then
        assertEquals(response.getPlaceId(), 1L);
        assertEquals(response.getAddress(), "테스트 주소");
        assertEquals(response.getPlaceName(), "테스트 장소명");
        assertEquals(response.getDescription(), "테스트 장소 설명");
        assertEquals(response.getPlaceType(), "관광지");
        assertNull(response.getCheckInTime());
        assertNull(response.getCheckOutTime());
        assertEquals(response.getHomepage(), "www.test.com");
        assertEquals(response.getPhoneNumber(), "010-0000-0000");
        assertEquals(response.getImageList().get(0).getImageName(), "test1.jpg");
        assertEquals(response.getImageList().get(1).getImageUrl(), "/test/test2.jpg");
    }

    @Test
    @DisplayName("getTravelDetails() 성공: 숙박 여행지 상세조회")
    void getTravelDetails_Lodging(){
        // given
        ApiContentType apiContentType = ApiContentType.builder().contentTypeId(1L).contentTypeName("숙박").build();
        TravelPlace travelPlace = createTravelPlace();
        travelPlace.setCheckInTime("13:00");
        travelPlace.setCheckOutTime("11:00");
        travelPlace.setApiContentType(apiContentType);

        when(travelRepository.findByPlaceId(1L))
                .thenReturn(Optional.of(travelPlace));

        // when
        TravelDetailResponse response = travelService.getTravelPlaceDetails(1L);

        // then
        assertEquals(response.getPlaceId(), 1L);
        assertEquals(response.getAddress(), "테스트 주소");
        assertEquals(response.getPlaceName(), "테스트 장소명");
        assertEquals(response.getDescription(), "테스트 장소 설명");
        assertEquals(response.getPlaceType(), "숙박");
        assertNull(response.getUseTime());
        assertEquals(response.getCheckInTime(), "13:00");
        assertEquals(response.getCheckOutTime(), "11:00");
        assertEquals(response.getHomepage(), "www.test.com");
        assertEquals(response.getPhoneNumber(), "010-0000-0000");
        assertEquals(response.getImageList().get(0).getImageName(), "test1.jpg");
        assertEquals(response.getImageList().get(1).getImageUrl(), "/test/test2.jpg");
    }

    @Test
    @DisplayName("getTravelDetails() 실패: 조회 시 데이터가 존재하지 않아 DataNotFoundException 발생")
    void getTravelDetails_DataNotFoundException(){
        // given
        when(travelRepository.findByPlaceId(1L))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> travelService.getTravelPlaceDetails(1L));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }




    /**
     * TravelPlace 객체 제공하는 메서드
     * @return TravelPlace
     */
    private TravelPlace createTravelPlace(){
        return TravelPlace.builder()
                .placeId(1L)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .createdAt(LocalDateTime.now())
                .description("테스트 장소 설명")
                .travelImageList(createTravelImages())
                .build();
    }


    /**
     * 거리 정보가 들어간 List<TravelLocationResponse> 제공하는 메소드
     * @return List<TravelLocationResponse>
     */
    private List<TravelLocationResponse> createTravelLocationResponses(){
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
    private List<TravelImage> createTravelImages(){
        ApiContentType apiContentType = ApiContentType.builder().contentTypeId(1L).contentTypeName("관광지").build();

        TravelPlace testTravel = TravelPlace.builder()
                .placeId(1L)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .placeName("테스트 장소명")
                .address("테스트 주소")
                .useTime("상시")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
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
                new TravelImage(1L, testTravel, testFile1),
                new TravelImage(2L, testTravel, testFile2)
        );
    }



}
