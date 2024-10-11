package com.triptune.domain.travel.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.travel.TravelTest;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.domain.common.repository.FileRepository;
import com.triptune.domain.travel.dto.*;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.PageableUtil;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelServiceTests extends TravelTest {

    @InjectMocks
    private TravelService travelService;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private TravelImageRepository travelImageRepository;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;
    private TravelPlace travelPlace;
    private File file;
    private List<TravelImage> imageList;


    @BeforeEach
    void setUp(){
        this.country = createCountry();
        this.city = createCity(country);
        this.district = createDistrict(city, "강남구");
        this.apiCategory = createApiCategory();
        this.travelPlace = createTravelPlace(country, city, district, apiCategory);
        this.file = createFile(1L, "test", true);

        this.imageList = new ArrayList<>();
        this.imageList.add(createTravelImage(travelPlace, file));
        this.travelPlace.setTravelImageList(imageList);
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void getNearByTravelPlaces_withData(){
        // given
        TravelLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        List<TravelLocation> locationList = new ArrayList<>();
        locationList.add(createTravelLocation(travelPlace));
        Page<TravelLocation> mockLocation = new PageImpl<>(locationList, pageable, 1);



        when(travelRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);
        when(travelImageRepository.findByTravelPlacePlaceId(any())).thenReturn(imageList);

        // when
        Page<TravelResponse> response = travelService.getNearByTravelPlaces(request, 1);

        // then
        List<TravelResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), locationList.get(0).getCity());
        assertEquals(content.get(0).getPlaceName(), locationList.get(0).getPlaceName());
        assertEquals(content.get(0).getAddress(), locationList.get(0).getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), imageList.get(0).getFile().getS3ObjectUrl());
        assertNotEquals(content.get(0).getDistance(), 0.0);

        System.out.println("전체 갯수: " + response.getTotalElements());
        System.out.println("전체 페이지수: " + response.getTotalPages());
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void getNearByTravelPlaces_noData(){
        // given
        TravelLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        Page<TravelLocation> mockLocation = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(travelRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);

        // when
        Page<TravelResponse> response = travelService.getNearByTravelPlaces(request, 1);

        // then
        assertEquals(response.getTotalElements(), 0);

        System.out.println("전체 갯수: " + response.getTotalElements());
        System.out.println("전체 페이지수: " + response.getTotalPages());
        System.out.println("내부 리스트 사이즈: " + response.getContent().size());

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 여행지 검색 결과가 존재하며 현재 위치에서 가까운 순으로 정렬")
    void searchTravelPlaces_withData(){
        // given
        TravelSearchRequest request = createTravelSearchRequest(37.49, 127.0, "테스트");

        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        List<TravelLocation> locationList = new ArrayList<>();
        locationList.add(createTravelLocation(travelPlace));
        Page<TravelLocation> mockLocation = new PageImpl<>(locationList, pageable, 1);

        List<TravelImage> imageList = new ArrayList<>();
        imageList.add(createTravelImage(travelPlace, file));

        when(travelRepository.searchTravelPlaces(pageable, request)).thenReturn(mockLocation);
        when(travelImageRepository.findByTravelPlacePlaceId(any())).thenReturn(imageList);

        // when
        Page<TravelResponse> response = travelService.searchTravelPlaces(request, 1);

        // then
        List<TravelResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), locationList.get(0).getCity());
        assertEquals(content.get(0).getPlaceName(), locationList.get(0).getPlaceName());
        assertEquals(content.get(0).getAddress(), locationList.get(0).getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), imageList.get(0).getFile().getS3ObjectUrl());
        assertNotEquals(content.get(0).getDistance(), 0.0);

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void searchTravelPlaces_noData(){
        // given
        TravelSearchRequest request = createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageableUtil.createPageRequest(1, 5);
        Page<TravelLocation> mockLocation = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(travelRepository.searchTravelPlaces(pageable, request)).thenReturn(mockLocation);

        // when
        Page<TravelResponse> response = travelService.searchTravelPlaces(request, 1);

        // then
        assertEquals(response.getTotalElements(), 0);

    }
    
    @Test
    @DisplayName("getTravelDetails() 성공: 숙박을 제외한 여행지 상세조회")
    void getTravelDetails_exceptLodging(){
        // given
        ApiContentType apiContentType = createApiContentType("관광지");
        travelPlace.setUseTime("상시");
        travelPlace.setApiContentType(apiContentType);

        when(travelRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace));

        // when
        TravelDetailResponse response = travelService.getTravelPlaceDetails(travelPlace.getPlaceId());

        // then
        assertEquals(response.getPlaceId(), travelPlace.getPlaceId());
        assertEquals(response.getAddress(), travelPlace.getAddress());
        assertEquals(response.getPlaceName(), travelPlace.getPlaceName());
        assertEquals(response.getDescription(), travelPlace.getDescription());
        assertEquals(response.getPlaceType(), travelPlace.getApiContentType().getContentTypeName());
        assertNull(response.getCheckInTime());
        assertNull(response.getCheckOutTime());
        assertEquals(response.getHomepage(), travelPlace.getHomepage());
        assertEquals(response.getPhoneNumber(), travelPlace.getPhoneNumber());
    }

    @Test
    @DisplayName("getTravelDetails() 성공: 숙박 여행지 상세조회")
    void getTravelDetails_Lodging(){
        // given
        ApiContentType apiContentType = createApiContentType("숙박");
        travelPlace.setCheckInTime("13:00");
        travelPlace.setCheckOutTime("11:00");
        travelPlace.setApiContentType(apiContentType);

        when(travelRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace));

        // when
        TravelDetailResponse response = travelService.getTravelPlaceDetails(travelPlace.getPlaceId());

        // then
        assertEquals(response.getPlaceId(), travelPlace.getPlaceId());
        assertEquals(response.getAddress(), travelPlace.getAddress());
        assertEquals(response.getPlaceName(), travelPlace.getPlaceName());
        assertEquals(response.getDescription(), travelPlace.getDescription());
        assertEquals(response.getPlaceType(), travelPlace.getApiContentType().getContentTypeName());
        assertNull(response.getUseTime());
        assertEquals(response.getCheckInTime(), travelPlace.getCheckInTime());
        assertEquals(response.getCheckOutTime(), travelPlace.getCheckOutTime());
        assertEquals(response.getHomepage(), travelPlace.getHomepage());
        assertEquals(response.getPhoneNumber(), travelPlace.getPhoneNumber());

    }

    @Test
    @DisplayName("getTravelDetails() 실패: 조회 시 데이터가 존재하지 않아 DataNotFoundException 발생")
    void getTravelDetails_DataNotFoundException(){
        // given
        when(travelRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> travelService.getTravelPlaceDetails(1L));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

}
