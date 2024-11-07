package com.triptune.domain.travel.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.travel.TravelTest;
import com.triptune.domain.travel.dto.PlaceLocation;
import com.triptune.domain.travel.dto.request.PlaceLocationRequest;
import com.triptune.domain.travel.dto.request.PlaceSearchRequest;
import com.triptune.domain.travel.dto.response.PlaceDetailResponse;
import com.triptune.domain.travel.dto.response.PlaceDistanceResponse;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelServiceTest extends TravelTest {

    @InjectMocks
    private TravelService travelService;

    @Mock
    private TravelPlaceRepository travelPlaceRepository;

    @Mock
    private TravelImageRepository travelImageRepository;

    private TravelPlace travelPlace;
    private TravelImage travelImage;


    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();
        this.travelPlace = createTravelPlace(1L, country, city, district, apiCategory);
        this.travelImage = createTravelImage(travelPlace, "test", true);
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void getNearByTravelPlaces_withData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtil.defaultPageable(1);

        List<TravelImage> imageList = new ArrayList<>(List.of(travelImage));
        travelPlace.setTravelImageList(imageList);
        List<PlaceLocation> locationList = new ArrayList<>(List.of(createTravelLocation(travelPlace)));

        Page<PlaceLocation> mockLocation = PageUtil.createPage(locationList, pageable, 1);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);
        when(travelImageRepository.findByTravelPlacePlaceId(anyLong())).thenReturn(imageList);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(request, 1);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), locationList.get(0).getCity());
        assertEquals(content.get(0).getPlaceName(), locationList.get(0).getPlaceName());
        assertEquals(content.get(0).getAddress(), locationList.get(0).getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), imageList.get(0).getS3ObjectUrl());
        assertNotEquals(content.get(0).getDistance(), 0.0);

        System.out.println("전체 갯수: " + response.getTotalElements());
        System.out.println("전체 페이지수: " + response.getTotalPages());
    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void getNearByTravelPlaces_noData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtil.defaultPageable(1);
        Page<PlaceLocation> mockLocation = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(request, 1);

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
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "테스트");

        Pageable pageable = PageUtil.defaultPageable(1);
        List<TravelImage> imageList = new ArrayList<>(List.of(travelImage));
        travelPlace.setTravelImageList(imageList);

        List<PlaceLocation> locationList = new ArrayList<>(List.of(createTravelLocation(travelPlace)));
        Page<PlaceLocation> mockLocation = PageUtil.createPage(locationList, pageable, 1);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockLocation);
        when(travelImageRepository.findByTravelPlacePlaceId(anyLong())).thenReturn(imageList);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlaces(request, 1);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), locationList.get(0).getCity());
        assertEquals(content.get(0).getPlaceName(), locationList.get(0).getPlaceName());
        assertEquals(content.get(0).getAddress(), locationList.get(0).getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), imageList.get(0).getS3ObjectUrl());
        assertNotEquals(content.get(0).getDistance(), 0.0);

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void searchTravelPlaces_noData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtil.defaultPageable(1);
        Page<PlaceLocation> mockLocation = PageUtil.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockLocation);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlaces(request, 1);

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

        List<TravelImage> imageList = new ArrayList<>(List.of(travelImage));
        travelPlace.setTravelImageList(imageList);

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace));

        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace.getPlaceId());

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

        List<TravelImage> imageList = new ArrayList<>(List.of(travelImage));
        travelPlace.setTravelImageList(imageList);

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace));


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace.getPlaceId());

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
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> travelService.getTravelPlaceDetails(1L));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

}
