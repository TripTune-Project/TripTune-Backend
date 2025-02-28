package com.triptune.travel.service;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.TravelTest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceDetailResponse;
import com.triptune.travel.dto.response.PlaceDistanceResponse;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelServiceTest extends TravelTest {

    @InjectMocks
    private TravelService travelService;

    @Mock
    private TravelPlaceRepository travelPlaceRepository;

    @Mock
    private TravelImageRepository travelImageRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    private TravelImage travelImage1;
    private TravelImage travelImage2;



    @BeforeEach
    void setUp(){
        country = createCountry();
        city = createCity(country);
        district = createDistrict(city, "강남구");
        apiCategory = createApiCategory();

        travelImage1 = createTravelImage(travelPlace1, "test", true);
        travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, 12.333, 160.3333, List.of(travelImage3, travelImage4));
    }


    @Test
    @DisplayName("로그인한 사용자의 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void getNearByTravelPlaces_loginAndExistsData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locationList = new ArrayList<>(List.of(createTravelLocation(travelPlace1), createTravelLocation(travelPlace2)));

        Page<PlaceLocation> mockLocation = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace1.getPlaceId())).thenReturn(travelImage1.getS3ObjectUrl());
        when(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId("member", travelPlace1.getPlaceId())).thenReturn(true);
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace2.getPlaceId())).thenReturn(null);
        when(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId("member", travelPlace2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, "member", request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isTrue();
        assertThat(content.get(0).getDistance()).isNotEqualTo(0.0);
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }




    @Test
    @DisplayName("익명 사용자의 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void getNearByTravelPlaces_anonymousAndExistsData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locationList = new ArrayList<>(List.of(createTravelLocation(travelPlace1), createTravelLocation(travelPlace2)));

        Page<PlaceLocation> mockLocation = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace1.getPlaceId())).thenReturn(travelImage1.getS3ObjectUrl());
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace2.getPlaceId())).thenReturn(null);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(0).getDistance()).isNotEqualTo(0.0);
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("로그인한 사용자의 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void getNearByTravelPlaces_loginAndNoData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockLocation = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, "member", request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("익명 사용자의 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void getNearByTravelPlaces_anonymousAndNoData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockLocation = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockLocation);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }


    @Test
    @DisplayName("로그인한 사용자의 여행지 검색 시 데이터 존재하는 경우")
    void searchTravelPlaces_WithLocation_loginAndExistsData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "테스트");

        Pageable pageable = PageUtils.defaultPageable(1);

        List<PlaceLocation> locationList = new ArrayList<>(List.of(createTravelLocation(travelPlace1), createTravelLocation(travelPlace2)));
        Page<PlaceLocation> mockLocation = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockLocation);
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace1.getPlaceId())).thenReturn(travelImage1.getS3ObjectUrl());
        when(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId("member", travelPlace1.getPlaceId())).thenReturn(true);
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace2.getPlaceId())).thenReturn(null);
        when(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId("member", travelPlace2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, "member", request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isTrue();
        assertThat(content.get(0).getDistance()).isNotEqualTo(0.0);
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("익명 사용자의 여행지 검색 시 데이터 존재하는 경우")
    void searchTravelPlaces_WithLocation_anonymousAndExistsData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "테스트");

        Pageable pageable = PageUtils.defaultPageable(1);

        List<PlaceLocation> locationList = new ArrayList<>(List.of(createTravelLocation(travelPlace1), createTravelLocation(travelPlace2)));
        Page<PlaceLocation> mockLocation = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockLocation);
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace1.getPlaceId())).thenReturn(travelImage1.getS3ObjectUrl());
        when(travelImageRepository.findThumbnailUrlByPlaceId(travelPlace2.getPlaceId())).thenReturn(null);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).getDistance()).isNotEqualTo(0.0);
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
    }


    @Test
    @DisplayName("로그인한 사용자의 여행지 검색 시 데이터 없는 경우")
    void searchTravelPlaces_WithLocation_loginAndNoData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockLocation = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockLocation);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, "member", request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }

    @Test
    @DisplayName("익명 사용자의 여행지 검색 시 데이터 없는 경우")
    void searchTravelPlaces_WithLocation_anonymousAndNoData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockLocation = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockLocation);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }

    @Test
    @DisplayName("로그인한 사용자의 숙박을 제외한 여행지 상세 조회")
    void getTravelDetails_loginAndExceptLodging(){
        // given
        ApiContentType apiContentType = createApiContentType("관광지");
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "상시", List.of(travelImage1, travelImage2));

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace1));
        when(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId(anyString(), anyLong())).thenReturn(true);

        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), "member");

        // then
        assertThat(response.getPlaceId()).isEqualTo(travelPlace1.getPlaceId());
        assertThat(response.getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(response.getDescription()).isEqualTo(travelPlace1.getDescription());
        assertThat(response.getPlaceType()).isEqualTo(travelPlace1.getApiContentType().getContentTypeName());
        assertThat(response.getCheckInTime()).isNull();
        assertThat(response.getCheckOutTime()).isNull();
        assertThat(response.getHomepage()).isEqualTo(travelPlace1.getHomepage());
        assertThat(response.getPhoneNumber()).isEqualTo(travelPlace1.getPhoneNumber());
        assertThat(response.isBookmarkStatus()).isTrue();
    }

    @Test
    @DisplayName("익명의 사용자의 숙박을 제외한 여행지 상세 조회")
    void getTravelDetails_anonymousAndExceptLodging(){
        // given
        ApiContentType apiContentType = createApiContentType("관광지");
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "상시", List.of(travelImage1, travelImage2));


        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace1));

        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), null);

        // then
        assertThat(response.getPlaceId()).isEqualTo(travelPlace1.getPlaceId());
        assertThat(response.getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(response.getDescription()).isEqualTo(travelPlace1.getDescription());
        assertThat(response.getPlaceType()).isEqualTo(travelPlace1.getApiContentType().getContentTypeName());
        assertThat(response.getCheckInTime()).isNull();
        assertThat(response.getCheckOutTime()).isNull();
        assertThat(response.getHomepage()).isEqualTo(travelPlace1.getHomepage());
        assertThat(response.getPhoneNumber()).isEqualTo(travelPlace1.getPhoneNumber());
        assertThat(response.isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("로그인한 사용자의 숙박 여행지 상세 조회")
    void getTravelDetails_loginAndLodging(){
        // given
        ApiContentType apiContentType = createApiContentType("숙박");
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "13:00", "11:00", List.of(travelImage1, travelImage2));

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace1));
        when(bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId(anyString(), anyLong())).thenReturn(true);


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), "member");

        // then
        assertThat(response.getPlaceId()).isEqualTo(travelPlace1.getPlaceId());
        assertThat(response.getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(response.getDescription()).isEqualTo(travelPlace1.getDescription());
        assertThat(response.getPlaceType()).isEqualTo(travelPlace1.getApiContentType().getContentTypeName());
        assertThat(response.getUseTime()).isNull();
        assertThat(response.getCheckInTime()).isEqualTo(travelPlace1.getCheckInTime());
        assertThat(response.getCheckOutTime()).isEqualTo(travelPlace1.getCheckOutTime());
        assertThat(response.getHomepage()).isEqualTo(travelPlace1.getHomepage());
        assertThat(response.getPhoneNumber()).isEqualTo(travelPlace1.getPhoneNumber());
        assertThat(response.isBookmarkStatus()).isTrue();

    }

    @Test
    @DisplayName("익명의 사용자의 숙박 여행지 상세 조회")
    void getTravelDetails_anonymousAndLodging(){
        // given
        ApiContentType apiContentType = createApiContentType("숙박");
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "13:00", "11:00", List.of(travelImage1, travelImage2));

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace1));


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), "member");

        // then
        assertThat(response.getPlaceId()).isEqualTo(travelPlace1.getPlaceId());
        assertThat(response.getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(response.getDescription()).isEqualTo(travelPlace1.getDescription());
        assertThat(response.getPlaceType()).isEqualTo(travelPlace1.getApiContentType().getContentTypeName());
        assertThat(response.getUseTime()).isNull();
        assertThat(response.getCheckInTime()).isEqualTo(travelPlace1.getCheckInTime());
        assertThat(response.getCheckOutTime()).isEqualTo(travelPlace1.getCheckOutTime());
        assertThat(response.getHomepage()).isEqualTo(travelPlace1.getHomepage());
        assertThat(response.getPhoneNumber()).isEqualTo(travelPlace1.getPhoneNumber());
        assertThat(response.isBookmarkStatus()).isFalse();

    }

    @Test
    @DisplayName("여행지 상세 조회 시 데이터가 존재하지 않아 예외 발생")
    void getTravelDetails_DataNotFoundException(){
        // given
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> travelService.getTravelPlaceDetails(1L, "member"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }



    @Test
    @DisplayName("중구 기준 여행지 조회")
    void getTravelPlacesByJungGu(){
        // given
        List<PlaceResponse> placeList = List.of(PlaceResponse.from(travelPlace1), PlaceResponse.from(travelPlace2));
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtils.createPage(placeList, pageable, 1));

        // when
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(1);

        // then
        assertEquals(response.getTotalElements(), placeList.size());
        assertEquals(response.getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("중구 기준 여행지 조회 시 여행지 데이터 없는 경우")
    void getTravelPlacesByJungGuWithoutData(){
        // given
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtils.createPage(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }


    @Test
    @DisplayName("여행지 검색")
    void searchTravelPlacesWithLocation(){
        // given
        String keyword = "중구";
        Pageable pageable = PageUtils.travelPageable(1);

        List<PlaceResponse> travelPlaceList = List.of(PlaceResponse.from(travelPlace1), PlaceResponse.from(travelPlace2));

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword))
                .thenReturn(PageUtils.createPage(travelPlaceList, pageable, travelPlaceList.size()));

        // when
        Page<PlaceResponse> response = travelService.searchTravelPlaces(1, keyword);


        // then
        List<PlaceResponse> content = response.getContent();
        assertEquals(content.get(0).getPlaceName(), travelPlace1.getPlaceName());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertNotNull(content.get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("여행지 검색 시 검색 결과 존재하지 않는 경우")
    void searchTravelPlacesWithLocationWithoutData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(PageUtils.createPage(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceResponse> response = travelService.searchTravelPlaces(1, keyword);


        // then
        assertEquals(response.getTotalElements(), 0);
    }


}
