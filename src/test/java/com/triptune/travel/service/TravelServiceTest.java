package com.triptune.travel.service;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
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
    private City seoul;
    private District gangnam;
    private ApiCategory apiCategory;

    private ApiContentType attractionContentType;
    private ApiContentType sportsContentType;
    private ApiContentType lodgingContentType;


    private Member member;

    @BeforeEach
    void setUp(){
        country = createCountry();
        seoul = createCity(country, "서울");
        gangnam = createDistrict(seoul, "강남구");
        apiCategory = createApiCategory();
        attractionContentType = createApiContentType(ThemeType.ATTRACTIONS);
        sportsContentType = createApiContentType(ThemeType.SPORTS);
        lodgingContentType = createApiContentType(ThemeType.LODGING);

        ProfileImage profileImage = createProfileImage("memberImage");
        member = createNativeTypeMember("member", profileImage);
    }


    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_member(){
        // given
        TravelPlace place1 = createTravelPlaceWithId(
                1L,
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createTravelPlaceWithIdAndLocation(
                2L,
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locations = List.of(
                createPlaceLocation(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceLocation(place2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, place1.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, place2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(1, 1L, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).isBookmarkStatus()).isTrue();
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).getDistance()).isNotNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_member_emptyResult(){
        // given
        PlaceLocationRequest request = createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceLocation> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

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
        TravelPlace place1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createTravelPlaceWithLocation(
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceLocationRequest request = createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locations = List.of(
                createPlaceLocation(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceLocation(place2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }



    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_nonMember_emptyResult(){
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
        TravelPlace place1 = createTravelPlaceWithId(
                1L,
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createTravelPlaceWithIdAndLocation(
                2L,
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locations = List.of(
                createPlaceLocation(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceLocation(place2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, place1.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, place2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithLocation(1, 1L, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).getDistance()).isNotNull();
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_member_emptyResult(){
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
        TravelPlace place1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createTravelPlaceWithLocation(
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceSearchRequest request = createTravelSearchRequest(37.49, 127.0, "여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceLocation> locations = List.of(
                createPlaceLocation(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceLocation(place2, null)
        );
        Page<PlaceLocation> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }


    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_nonMember_emptyResult(){
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
        TravelPlace place1 = createTravelPlaceWithId(
                1L,
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createTravelPlaceWithIdAndLocation(
                2L,
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceSearchRequest request = createTravelSearchRequest("여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceResponse> locations = List.of(
                createPlaceResponse(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceResponse(place2, null)
        );
        Page<PlaceResponse> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.searchTravelPlaces(pageable, request.getKeyword())).thenReturn(mockResponse);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, place1.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, place2.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithoutLocation(1, 1L, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("회원의 위치를 기반하지 않고 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_member_emptyResult(){
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
        TravelPlace place1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createTravelPlaceWithLocation(
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceSearchRequest request = createTravelSearchRequest("여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceResponse> locations = List.of(
                createPlaceResponse(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceResponse(place2, null)
        );
        Page<PlaceResponse> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.searchTravelPlaces(pageable, request.getKeyword())).thenReturn(mockResponse);

        // when
        Page<PlaceLocation> response = travelService.searchTravelPlacesWithoutLocation(1, null, request);

        // then
        List<PlaceLocation> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_nonMember_emptyResult(){
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
        TravelPlace attractionPlace = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        createTravelImage(attractionPlace, "test1", true);
        createTravelImage(attractionPlace, "test2", false);

        when(travelPlaceRepository.findById(1L)).thenReturn(Optional.of(attractionPlace));
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, 1L)).thenReturn(true);

        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(1L, 1L);

        // then
        assertThat(response.getPlaceName()).isEqualTo(attractionPlace.getPlaceName());
        assertThat(response.getPlaceType()).isEqualTo(attractionPlace.getApiContentType().getContentTypeName());
        assertThat(response.getCheckInTime()).isNull();
        assertThat(response.getCheckOutTime()).isNull();
        assertThat(response.isBookmarkStatus()).isTrue();
        assertThat(response.getImageList()).hasSize(2);
    }

    @Test
    @DisplayName("비회원의 숙박을 제외한 여행지 상세 조회")
    void getTravelDetails_nonMemberAndExceptLodging(){
        // given
        TravelPlace attractionPlace = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        createTravelImage(attractionPlace, "test1", true);
        createTravelImage(attractionPlace, "test2", false);

        when(travelPlaceRepository.findById(1L)).thenReturn(Optional.of(attractionPlace));

        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(1L, null);

        // then
        assertThat(response.getPlaceName()).isEqualTo(attractionPlace.getPlaceName());
        assertThat(response.getPlaceType()).isEqualTo(attractionPlace.getApiContentType().getContentTypeName());
        assertThat(response.getCheckInTime()).isNull();
        assertThat(response.getCheckOutTime()).isNull();
        assertThat(response.isBookmarkStatus()).isFalse();
        assertThat(response.getImageList()).hasSize(2);
    }

    @Test
    @DisplayName("회원의 숙박 여행지 상세 조회")
    void getTravelDetails_memberAndLodging(){
        // given
        TravelPlace lodgingPlace = createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );
        createTravelImage(lodgingPlace, "test1", true);

        when(travelPlaceRepository.findById(1L)).thenReturn(Optional.of(lodgingPlace));
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, 1L)).thenReturn(true);


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(1L, 1L);

        // then
        assertThat(response.getPlaceName()).isEqualTo(lodgingPlace.getPlaceName());
        assertThat(response.getDescription()).isEqualTo(lodgingPlace.getDescription());
        assertThat(response.getPlaceType()).isEqualTo(lodgingPlace.getApiContentType().getContentTypeName());
        assertThat(response.getUseTime()).isNull();
        assertThat(response.getCheckInTime()).isEqualTo(lodgingPlace.getCheckInTime());
        assertThat(response.getCheckOutTime()).isEqualTo(lodgingPlace.getCheckOutTime());
        assertThat(response.isBookmarkStatus()).isTrue();
        assertThat(response.getImageList()).hasSize(1);

    }

    @Test
    @DisplayName("비회원의 숙박 여행지 상세 조회")
    void getTravelDetails_nonMemberAndLodging(){
        // given
        TravelPlace lodgingPlace = createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );
        createTravelImage(lodgingPlace, "test1", true);

        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(lodgingPlace));


        // when
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(2L, member.getMemberId());

        // then
        assertThat(response.getPlaceName()).isEqualTo(lodgingPlace.getPlaceName());
        assertThat(response.getDescription()).isEqualTo(lodgingPlace.getDescription());
        assertThat(response.getPlaceType()).isEqualTo(lodgingPlace.getApiContentType().getContentTypeName());
        assertThat(response.getUseTime()).isNull();
        assertThat(response.getCheckInTime()).isEqualTo(lodgingPlace.getCheckInTime());
        assertThat(response.getCheckOutTime()).isEqualTo(lodgingPlace.getCheckOutTime());
        assertThat(response.isBookmarkStatus()).isFalse();
        assertThat(response.getImageList()).hasSize(1);

    }

    @Test
    @DisplayName("여행지 상세 조회 시 데이터가 존재하지 않아 예외 발생")
    void getTravelDetails_placeNotFound(){
        // given
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelService.getTravelPlaceDetails(1000L, member.getMemberId()));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }



    @Test
    @DisplayName("중구 기준 여행지 조회")
    void getTravelPlacesByJungGu(){
        // given
        TravelPlace place1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        Pageable pageable = PageUtils.travelPageable(1);
        List<PlaceResponse> mockResponse = List.of(
                createPlaceResponse(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceResponse(place2, null)
        );

        when(travelPlaceRepository.findDefaultTravelPlacesByJungGu(any()))
                .thenReturn(PageUtils.createPage(mockResponse, pageable, mockResponse.size()));

        // when
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(1);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("중구 기준 여행지 조회 시 여행지 데이터 없는 경우")
    void getTravelPlacesByJungGuWithoutData(){
        // given
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.findDefaultTravelPlacesByJungGu(any()))
                .thenReturn(PageUtils.createPage(Collections.emptyList(), pageable, 0));

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
        String keyword = "여행";

        TravelPlace place1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage place1Thumbnail = createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        TravelPlace place2 = createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        Pageable pageable = PageUtils.travelPageable(1);
        List<PlaceResponse> mockResponse = List.of(
                createPlaceResponse(place1, place1Thumbnail.getS3ObjectUrl()),
                createPlaceResponse(place2, null)
        );

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword))
                .thenReturn(PageUtils.createPage(mockResponse, pageable, mockResponse.size()));

        // when
        Page<PlaceResponse> response = travelService.searchTravelPlaces(1, keyword);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(place1.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(place1.getAddress());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1Thumbnail.getS3ObjectUrl());
        assertThat(content.get(1).getPlaceName()).isEqualTo(place2.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("여행지 검색 시 검색 결과 존재하지 않는 경우")
    void searchTravelPlacesWithLocationWithoutData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword))
                .thenReturn(PageUtils.createPage(Collections.emptyList(), pageable, 0));

        // when
        Page<PlaceResponse> response = travelService.searchTravelPlaces(1, keyword);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("인기 여행지 조회 - 전체")
    void findPopularTravelPlacesByCity_ALL(){
        // given
        TravelPlace gangnamPlace1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage gangnam1Thumbnail = createTravelImage(gangnamPlace1, "test1", true);
        createTravelImage(gangnamPlace1, "test2", false);

        TravelPlace gangnamPlace2 = createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        City busan = createCity(country, "부산");
        District busanDistrict = createDistrict(busan, "금정구");
        TravelPlace busanPlace = createTravelPlace(
                country,
                busan,
                busanDistrict,
                apiCategory,
                attractionContentType,
                "금정 여행지"
        );
        TravelImage busanThumbnail = createTravelImage(busanPlace, "부산이미지1", true);

        City jeolla = createCity(country, "전라남도");
        District jeollaDistrict = createDistrict(busan, "보성구");
        TravelPlace jeollaPlace = createTravelPlace(
                country,
                jeolla,
                jeollaDistrict,
                apiCategory,
                attractionContentType,
                "보성 여행지"
        );

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(jeollaPlace, null),
                createPlaceSimpleResponse(busanPlace, busanThumbnail.getS3ObjectUrl()),
                createPlaceSimpleResponse( gangnamPlace1, gangnam1Thumbnail.getS3ObjectUrl()),
                createPlaceSimpleResponse(gangnamPlace2, null)
        );

        when(travelPlaceRepository.findPopularTravelPlaces(CityType.ALL)).thenReturn(mockResult);

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlaces(CityType.ALL);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(busanPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanThumbnail.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceName()).isEqualTo(gangnamPlace1.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(gangnam1Thumbnail.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceName()).isEqualTo(gangnamPlace2.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("인기 여행지 조회 - 전라도")
    void findPopularTravelPlacesByCity_JEOLLA(){
        // given
        City jeolla1 = createCity(country, "전북특별자치도");
        District jeolla1District = createDistrict(jeolla1, "고창군");
        TravelPlace jeollaPlace1 = createTravelPlace(
                country,
                jeolla1,
                jeolla1District,
                apiCategory,
                attractionContentType,
                "고창 여행지"
        );
        TravelImage jeolla1Thumbnail = createTravelImage(jeollaPlace1, "부산이미지1", true);

        City jeolla2 = createCity(country, "전라남도");
        District jeolla2District = createDistrict(jeolla2, "보성구");
        TravelPlace jeollaPlace2 = createTravelPlace(
                country,
                jeolla2,
                jeolla2District,
                apiCategory,
                attractionContentType,
                "보성 여행지"
        );

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(jeollaPlace2, null),
                createPlaceSimpleResponse(jeollaPlace1, jeolla1Thumbnail.getS3ObjectUrl())
        );

        when(travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA)).thenReturn(mockResult);

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(jeollaPlace1.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(jeolla1Thumbnail.getS3ObjectUrl());
    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty(){
        // given
        when(travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA)).thenReturn(Collections.emptyList());

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL(){
        // given
        TravelPlace attractionPlace1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage attraction1Thumbnail = createTravelImage(attractionPlace1, "test1", true);
        createTravelImage(attractionPlace1, "test2", false);

        TravelPlace lodgingPlace = createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        City busan = createCity(country, "부산");
        District busanDistrict = createDistrict(busan, "금정구");
        TravelPlace sportsPlace = createTravelPlace(
                country,
                busan,
                busanDistrict,
                apiCategory,
                sportsContentType,
                "부산 여행지"
        );
        TravelImage sportsThumbnail = createTravelImage(sportsPlace, "부산이미지1", true);

        City jeolla = createCity(country, "전라남도");
        District jeollaDistrict = createDistrict(busan, "보성구");
        TravelPlace attractionPlace2 = createTravelPlace(
                country,
                jeolla,
                jeollaDistrict,
                apiCategory,
                attractionContentType,
                "전라도 여행지"
        );

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(attractionPlace2, null),
                createPlaceSimpleResponse(sportsPlace, sportsThumbnail.getS3ObjectUrl()),
                createPlaceSimpleResponse(attractionPlace1, attraction1Thumbnail.getS3ObjectUrl()),
                createPlaceSimpleResponse(lodgingPlace, null)
        );

        when(travelPlaceRepository.findRecommendTravelPlaces(ThemeType.All)).thenReturn(mockResult);


        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlaces(ThemeType.All);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(attractionPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(sportsPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(sportsThumbnail.getS3ObjectUrl());
        assertThat(response.get(2).getPlaceName()).isEqualTo(attractionPlace1.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(attraction1Thumbnail.getS3ObjectUrl());
        assertThat(response.get(3).getPlaceName()).isEqualTo(lodgingPlace.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("추천 테마 여행지 조회 - 관광지")
    void findRecommendTravelPlacesByTheme_ATTRACTIONS(){
        // given
        TravelPlace attractionPlace1 = createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage attraction1Thumbnail = createTravelImage(attractionPlace1, "test1", true);
        createTravelImage(attractionPlace1, "test2", false);

        City jeolla2 = createCity(country, "전라남도");
        District jeolla2District = createDistrict(jeolla2, "보성구");
        TravelPlace attractionPlace2 = createTravelPlace(
                country,
                jeolla2,
                jeolla2District,
                apiCategory,
                attractionContentType,
                "전라도 여행지"
        );

        List<PlaceSimpleResponse> mockResult = List.of(
                createPlaceSimpleResponse(attractionPlace2, null),
                createPlaceSimpleResponse(attractionPlace1, attraction1Thumbnail.getS3ObjectUrl())
        );

        when(travelPlaceRepository.findRecommendTravelPlaces(ThemeType.ATTRACTIONS)).thenReturn(mockResult);

        // when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlaces(ThemeType.ATTRACTIONS);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(attractionPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(attractionPlace1.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(attraction1Thumbnail.getS3ObjectUrl());

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty(){
        // given, when
        List<PlaceSimpleResponse> response = travelPlaceRepository.findRecommendTravelPlaces(ThemeType.FOOD);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


}
