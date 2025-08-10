package com.triptune.travel.service;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.travel.TravelTest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceDetailResponse;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelServiceTest extends TravelTest {

    @InjectMocks private TravelService travelService;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private BookmarkRepository bookmarkRepository;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    private TravelImage travelImage1;
    private TravelImage travelImage2;

    private ApiContentType attractionContentType;

    private Member member;

    @BeforeEach
    void setUp(){
        country = createCountry();
        city = createCity(country);
        district = createDistrict(city, "강남구");
        apiCategory = createApiCategory();
        attractionContentType = createApiContentType(ThemeType.ATTRACTIONS);
      
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, attractionContentType, 0);
          
        travelImage1 = createTravelImage(travelPlace1, "test1", true);
        travelImage2 = createTravelImage(travelPlace1, "test2", false);

        ApiContentType sportsContentType = createApiContentType(ThemeType.SPORTS);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, sportsContentType, 12.333, 160.3333);

        member = createMember(1L, "member");

    }


    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_member(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locationList = List.of(
                createPlaceLocation(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceLocation(travelPlace2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace1.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(1, member.getMemberId(), request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isTrue();
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_memberAndNoData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockResponse = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(1, member.getMemberId(), request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_nonMember(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locationList = List.of(
                createPlaceLocation(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceLocation(travelPlace2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }



    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_nonMemberAndNoData(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }


    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithLocation_member(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "테스트");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locationList = List.of(
                createPlaceLocation(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceLocation(travelPlace2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace1.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithLocation(1, member.getMemberId(), request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isTrue();
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_memberAndNoData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithLocation(1, member.getMemberId(), request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithLocation_nonMember(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "테스트");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locationList = List.of(
                createPlaceLocation(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceLocation(travelPlace2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }


    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_nonMemberAndNoData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }


    @Test
    @DisplayName("회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_member(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest("테스트");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceResponse> locationList = List.of(
                createPlaceResponse(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceResponse(travelPlace2, null)
        );
        Page<PlaceResponse> mockResponse = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.searchTravelPlaces(pageable, request.getKeyword())).thenReturn(mockResponse);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace1.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(member.getMemberId(), travelPlace2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithoutLocation(1, member.getMemberId(), request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isTrue();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("회원의 위치를 기반하지 않고 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_memberAndNoData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest("ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceResponse> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlaces(pageable, request.getKeyword())).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithoutLocation(1, member.getMemberId(), request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }

    @Test
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_nonMember(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest("테스트");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceResponse> locationList = List.of(
                createPlaceResponse(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceResponse(travelPlace2, null)
        );
        Page<PlaceResponse> mockResponse = PageUtils.createPage(locationList, pageable, locationList.size());

        when(travelPlaceRepository.searchTravelPlaces(pageable, request.getKeyword())).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithoutLocation(1, null, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getCity()).isEqualTo(travelPlace1.getCity().getCityName());
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_nonMemberAndNoData(){
        // given
        PlaceSearchRequest request = createTravelSearchRequest("ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceResponse> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlaces(pageable, request.getKeyword())).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithoutLocation(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("회원의 숙박을 제외한 여행지 상세 조회")
    void getTravelDetails_memberAndExceptLodging(){
        // given
        ApiContentType apiContentType = createApiContentType(ThemeType.ATTRACTIONS);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "상시", List.of(travelImage1, travelImage2));

        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(travelPlace1));
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);

        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), member.getMemberId());

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
    @DisplayName("비회원의 숙박을 제외한 여행지 상세 조회")
    void getTravelDetails_nonMemberAndExceptLodging(){
        // given
        ApiContentType apiContentType = createApiContentType(ThemeType.ATTRACTIONS);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "상시", List.of(travelImage1, travelImage2));


        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(travelPlace1));

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
    @DisplayName("회원의 숙박 여행지 상세 조회")
    void getTravelDetails_memberAndLodging(){
        // given
        ApiContentType apiContentType = createApiContentType(ThemeType.LODGING);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "13:00", "11:00", List.of(travelImage1, travelImage2));

        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(travelPlace1));
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), member.getMemberId());

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
    @DisplayName("비회원의 숙박 여행지 상세 조회")
    void getTravelDetails_nonMemberAndLodging(){
        // given
        ApiContentType apiContentType = createApiContentType(ThemeType.ATTRACTIONS);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, apiContentType, "13:00", "11:00", List.of(travelImage1, travelImage2));

        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(travelPlace1));


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(travelPlace1.getPlaceId(), member.getMemberId());

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
    void getTravelDetails_placeNotFound(){
        // given
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelService.getTravelPlaceDetails(1L, member.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.DATA_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.DATA_NOT_FOUND.getMessage());
    }



    @Test
    @DisplayName("중구 기준 여행지 조회")
    void getTravelPlacesByJungGu(){
        // given
        Pageable pageable = PageUtils.travelPageable(1);
        List<PlaceResponse> mockResponse = List.of(
                createPlaceResponse(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceResponse(travelPlace2, null)
        );

        when(travelPlaceRepository.findDefaultTravelPlacesByJungGu(any()))
                .thenReturn(PageUtils.createPage(mockResponse, pageable, 1));

        // when
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(1);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(mockResponse.size());
        assertThat(content.get(0).getPlaceName()).isEqualTo(mockResponse.get(0).getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("중구 기준 여행지 조회 시 여행지 데이터 없는 경우")
    void getTravelPlacesByJungGuWithoutData(){
        // given
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.findDefaultTravelPlacesByJungGu(any()))
                .thenReturn(PageUtils.createPage(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(1);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("여행지 검색")
    void searchTravelPlacesWithLocation(){
        // given
        String keyword = "중구";

        Pageable pageable = PageUtils.travelPageable(1);
        List<PlaceResponse> mockResponse = List.of(
                createPlaceResponse(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceResponse(travelPlace2, null)
        );

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword))
                .thenReturn(PageUtils.createPage(mockResponse, pageable, mockResponse.size()));

        // when
        Page<PlaceResponse> response = travelService.searchTravelPlaces(1, keyword);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(travelPlace1.getAddress());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
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
        assertThat(response.getTotalElements()).isEqualTo(0);
    }


    @Test
    @DisplayName("인기 여행지 조회 - 전체")
    void findPopularTravelPlacesByCity_ALL(){
        // given
        City busan = createCity(country, "부산");
        District busanDistrict = createDistrict(busan, "금정구");
        TravelPlace travelPlace3 = createTravelPlace(3L, country, busan, busanDistrict, apiCategory, "금정 여행지", 5);
        TravelImage busanImage1 = createTravelImage(travelPlace3, "부산이미지1", true);

        City jeolla = createCity(country, "전라남도");
        District jeollaDistrict = createDistrict(busan, "보성구");
        TravelPlace travelPlace4 = createTravelPlace(4L, country, jeolla, jeollaDistrict, apiCategory, "보성 여행지", 10);

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(travelPlace4, null),
                createPlaceSimpleResponse(travelPlace3, busanImage1.getS3ObjectUrl()),
                createPlaceSimpleResponse(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceSimpleResponse(travelPlace2, null)
        );

        when(travelPlaceRepository.findPopularTravelPlacesByCity(CityType.ALL)).thenReturn(mockResult);

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlacesByCity(CityType.ALL);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(travelPlace4.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(travelPlace3.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanImage1.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceName()).isEqualTo(travelPlace2.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("인기 여행지 조회 - 전라도")
    void findPopularTravelPlacesByCity_JEOLLA(){
        // given
        City jeolla1 = createCity(country, "전북특별자치도");
        District jeolla1District = createDistrict(jeolla1, "고창군");
        TravelPlace travelPlace3 = createTravelPlace(3L, country, jeolla1, jeolla1District, apiCategory, "고창 여행지", 5);
        TravelImage jeolla1Image1 = createTravelImage(travelPlace3, "부산이미지1", true);

        City jeolla2 = createCity(country, "전라남도");
        District jeolla2District = createDistrict(jeolla2, "보성구");
        TravelPlace travelPlace4 = createTravelPlace(4L, country, jeolla2, jeolla2District, apiCategory, "보성 여행지", 10);

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(travelPlace4, null),
                createPlaceSimpleResponse(travelPlace3, jeolla1Image1.getS3ObjectUrl())
        );

        when(travelPlaceRepository.findPopularTravelPlacesByCity(CityType.JEOLLA)).thenReturn(mockResult);

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlacesByCity(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(travelPlace4.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(travelPlace3.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(jeolla1Image1.getS3ObjectUrl());
    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty(){
        // given
        when(travelPlaceRepository.findPopularTravelPlacesByCity(CityType.JEOLLA)).thenReturn(new ArrayList<>());

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlacesByCity(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL(){
        // given
        City busan = createCity(country, "부산");
        District busanDistrict = createDistrict(busan, "금정구");
        ApiContentType cultureContentType = createApiContentType(ThemeType.CULTURE);
        TravelPlace travelPlace3 = createTravelPlace(null, country, busan, busanDistrict, apiCategory, cultureContentType, 5);
        TravelImage busanImage1 = createTravelImage(travelPlace3, "부산이미지1", true);

        City jeolla = createCity(country, "전라남도");
        District jeollaDistrict = createDistrict(busan, "보성구");
        TravelPlace travelPlace4 = createTravelPlace(null, country, jeolla, jeollaDistrict, apiCategory, attractionContentType, 10);

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(travelPlace4, null),
                createPlaceSimpleResponse(travelPlace3, busanImage1.getS3ObjectUrl()),
                createPlaceSimpleResponse(travelPlace1, travelImage1.getS3ObjectUrl()),
                createPlaceSimpleResponse(travelPlace2, null)
        );

        when(travelPlaceRepository.findRecommendTravelPlacesByTheme(ThemeType.All)).thenReturn(mockResult);


        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlacesByTheme(ThemeType.All);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceId()).isEqualTo(travelPlace4.getPlaceId());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceId()).isEqualTo(travelPlace3.getPlaceId());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanImage1.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceId()).isEqualTo(travelPlace1.getPlaceId());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceId()).isEqualTo(travelPlace2.getPlaceId());
        assertThat(response.get(3).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("추천 테마 여행지 조회 - 관광지")
    void findRecommendTravelPlacesByTheme_ATTRACTIONS(){
        // given
        City jeolla2 = createCity(country, "전라남도");
        District jeolla2District = createDistrict(jeolla2, "보성구");
        TravelPlace travelPlace4 = createTravelPlace(null, country, jeolla2, jeolla2District, apiCategory, attractionContentType, 10);

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(travelPlace4, null),
                createPlaceSimpleResponse(travelPlace1, travelImage1.getS3ObjectUrl())
        );

        when(travelPlaceRepository.findRecommendTravelPlacesByTheme(ThemeType.ATTRACTIONS)).thenReturn(mockResult);

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlacesByTheme(ThemeType.ATTRACTIONS);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceId()).isEqualTo(travelPlace4.getPlaceId());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceId()).isEqualTo(travelPlace1.getPlaceId());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(travelImage1.getS3ObjectUrl());

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty(){
        // given
        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlacesByTheme(ThemeType.FOOD);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


}
