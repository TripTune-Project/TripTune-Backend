package com.triptune.travel.service;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.travel.dto.response.*;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.travel.repository.dto.PlaceDistanceQueryDto;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import com.triptune.travel.repository.dto.PlaceSimpleQueryDto;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelServiceTest  {
    @InjectMocks private TravelService travelService;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private S3ObjectManager s3ObjectManager;

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
        country = CountryFixture.createCountry();
        seoul = CityFixture.createCity(country, "서울");
        gangnam = DistrictFixture.createDistrict(seoul, "강남구");
        apiCategory = ApiCategoryFixture.createApiCategory();
        attractionContentType = ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS);
        sportsContentType = ApiContentTypeFixture.createApiContentType(ThemeType.SPORTS);
        lodgingContentType = ApiContentTypeFixture.createApiContentType(ThemeType.LODGING);

        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        member = MemberFixture.createNativeTypeMember("member", profileImage);
    }


    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_member(){
        // given
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithIdAndLocation(
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

        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceDistanceQueryDto> places = List.of(
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(places, pageable, places.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, placeWithThumb.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, placeWithoutThumb.getPlaceId())).thenReturn(false);


        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, 1L, request);


        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);

        PlaceDistanceResponse first = content.get(0);
        PlaceDistanceResponse second = content.get(1);

        assertThat(first.getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(first.getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(first.getDistance()).isNotNull();
        assertThat(first.isBookmarkStatus()).isTrue();

        assertThat(second.getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(second.getThumbnailUrl()).isNull();
        assertThat(second.getDistance()).isNotNull();
        assertThat(second.isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_member_emptyResult(){
        // given
        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, member.getMemberId(), request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회")
    void getNearByTravelPlaces_nonMember(){
        // given
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(37.4970465429, 127.0281573537);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceDistanceQueryDto> places = List.of(
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(places, pageable, places.size());

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);

        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(0).getDistance()).isNotNull();

        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }



    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지 목록을 조회할 때, 데이터가 없는 경우")
    void getNearByTravelPlaces_nonMember_emptyResult(){
        // given
        PlaceLocationRequest request = TravelPlaceFixture.createTravelLocationRequest(0.0, 0.0);

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.findNearByTravelPlaces(pageable, request, 5)).thenReturn(mockResponse);

        // when
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }


    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithLocation_member(){
        // given
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithIdAndLocation(
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

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.49, 127.0, "여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceDistanceQueryDto> places = List.of(
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(places, pageable, places.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, placeWithThumb.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, placeWithoutThumb.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, 1L, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).getDistance()).isNotNull();
    }

    @Test
    @DisplayName("회원의 위치를 기반으로 여행지 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_member_emptyResult(){
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, member.getMemberId(), request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }

    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithLocation_nonMember(){
        // given
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.49, 127.0, "여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceDistanceQueryDto> locations = List.of(
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(0).getDistance()).isNotNull();
        assertThat(content.get(0).isBookmarkStatus()).isFalse();

        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getDistance()).isNotNull();
    }


    @Test
    @DisplayName("비회원의 위치를 기반으로 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithLocation_nonMember_emptyResult(){
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest(37.49, 127.0, "ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithLocation(pageable, request)).thenReturn(mockResponse);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithLocation(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }


    @Test
    @DisplayName("회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_member(){
        // given
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithIdAndLocation(
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

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceDistanceQueryDto> places = List.of(
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(places, pageable, places.size());

        when(travelPlaceRepository.searchTravelPlacesWithoutLocation(pageable, request.getKeyword())).thenReturn(mockResponse);
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, placeWithThumb.getPlaceId())).thenReturn(true);
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(1L, placeWithoutThumb.getPlaceId())).thenReturn(false);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithoutLocation(1, 1L, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("회원의 위치를 기반하지 않고 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_member_emptyResult(){
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithoutLocation(pageable, request.getKeyword())).thenReturn(mockResponse);


        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithoutLocation(1, member.getMemberId(), request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);

    }

    @Test
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하는 경우")
    void searchTravelPlacesWithoutLocation_nonMember(){
        // given
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithLocation(
                country,
                seoul,
                gangnam,
                apiCategory,
                sportsContentType,
                "여행지2",
                12.333,
                160.3333
        );

        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("여행지");

        Pageable pageable = PageUtils.defaultPageable(1);
        List<PlaceDistanceQueryDto> locations = List.of(
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceDistanceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(locations, pageable, locations.size());

        when(travelPlaceRepository.searchTravelPlacesWithoutLocation(pageable, request.getKeyword())).thenReturn(mockResponse);
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(null)).thenReturn(null);


        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithoutLocation(1, null, request);

        // then
        List<PlaceDistanceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(0).isBookmarkStatus()).isFalse();
        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
        assertThat(content.get(1).isBookmarkStatus()).isFalse();
    }

    @Test
    @DisplayName("비회원의 위치를 기반하지 않고 여행지를 검색할 때, 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutLocation_nonMember_emptyResult(){
        // given
        PlaceSearchRequest request = TravelPlaceFixture.createTravelSearchRequest("ㅁㄴㅇㄹ");

        Pageable pageable = PageUtils.defaultPageable(1);
        Page<PlaceDistanceQueryDto> mockResponse = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlacesWithoutLocation(pageable, request.getKeyword())).thenReturn(mockResponse);

        // when
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlacesWithoutLocation(1, null, request);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("회원의 숙박을 제외한 여행지 상세 조회")
    void getTravelDetails_memberAndExceptLodging(){
        // given
        TravelPlace attractionPlace = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImageFixture.createTravelImage(attractionPlace, "test1", true);
        TravelImageFixture.createTravelImage(attractionPlace, "test2", false);

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
        TravelPlace attractionPlace = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImageFixture.createTravelImage(attractionPlace, "test1", true);
        TravelImageFixture.createTravelImage(attractionPlace, "test2", false);

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
        TravelPlace lodgingPlace = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );
        TravelImageFixture.createTravelImage(lodgingPlace, "test1", true);

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
        TravelPlace lodgingPlace = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );
        TravelImageFixture.createTravelImage(lodgingPlace, "test1", true);

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
        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        Pageable pageable = PageUtils.travelPageable(1);
        List<PlaceQueryDto> mockResponse = List.of(
                TravelPlaceFixture.createPlaceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceQueryDto(placeWithoutThumb, null)
        );

        when(travelPlaceRepository.findNearbyTravelPlacesFromJungGu(any()))
                .thenReturn(PageUtils.createPage(mockResponse, pageable, mockResponse.size()));
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);

        // when
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(1);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("중구 기준 여행지 조회 시 여행지 데이터 없는 경우")
    void getTravelPlacesByJungGuWithoutData(){
        // given
        Pageable pageable = PageUtils.travelPageable(1);

        when(travelPlaceRepository.findNearbyTravelPlacesFromJungGu(any()))
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

        TravelPlace placeWithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage placeThumb = TravelImageFixture.createTravelImage(placeWithThumb, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumb, "test2", false);
        String placeThumbUrl = S3Fixture.createS3ObjectUrl(placeThumb.getS3ObjectKey());

        TravelPlace placeWithoutThumb = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        Pageable pageable = PageUtils.travelPageable(1);
        List<PlaceQueryDto> mockResponse = List.of(
                TravelPlaceFixture.createPlaceQueryDto(placeWithThumb, placeThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceQueryDto(placeWithoutThumb, null)
        );

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword))
                .thenReturn(PageUtils.createPage(mockResponse, pageable, mockResponse.size()));
        when(s3ObjectManager.generateS3ObjectUrl(placeThumb.getS3ObjectKey())).thenReturn(placeThumbUrl);

        // when
        Page<PlaceResponse> response = travelService.searchTravelPlaces(1, keyword);

        // then
        List<PlaceResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getPlaceName()).isEqualTo(placeWithThumb.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(placeWithThumb.getAddress());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(placeThumbUrl);
        assertThat(content.get(1).getPlaceName()).isEqualTo(placeWithoutThumb.getPlaceName());
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
        TravelPlace gangnamPlace1 = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage gangnam1Thumb = TravelImageFixture.createTravelImage(gangnamPlace1, "test1", true);
        TravelImageFixture.createTravelImage(gangnamPlace1, "test2", false);
        String gangnam1ThumbUrl = S3Fixture.createS3ObjectUrl(gangnam1Thumb.getS3ObjectKey());

        TravelPlace gangnamPlace2 = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        City busan = CityFixture.createCity(country, "부산");
        District busanDistrict = DistrictFixture.createDistrict(busan, "금정구");
        TravelPlace busanPlace = TravelPlaceFixture.createTravelPlace(
                country,
                busan,
                busanDistrict,
                apiCategory,
                attractionContentType,
                "금정 여행지"
        );
        TravelImage busanThumb = TravelImageFixture.createTravelImage(busanPlace, "부산이미지1", true);
        String busanThumbUrl = S3Fixture.createS3ObjectUrl(busanThumb.getS3ObjectKey());

        City jeolla = CityFixture.createCity(country, "전라남도");
        District jeollaDistrict = DistrictFixture.createDistrict(busan, "보성구");
        TravelPlace jeollaPlace = TravelPlaceFixture.createTravelPlace(
                country,
                jeolla,
                jeollaDistrict,
                apiCategory,
                attractionContentType,
                "보성 여행지"
        );

        List<PlaceSimpleQueryDto> mockResult = List.of(
                TravelPlaceFixture.createPlaceSimpleQueryDto(jeollaPlace, null),
                TravelPlaceFixture.createPlaceSimpleQueryDto(busanPlace, busanThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceSimpleQueryDto(gangnamPlace1, gangnam1Thumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceSimpleQueryDto(gangnamPlace2, null)
        );

        when(travelPlaceRepository.findPopularTravelPlaces(CityType.ALL)).thenReturn(mockResult);
        when(s3ObjectManager.generateS3ObjectUrl(busanThumb.getS3ObjectKey())).thenReturn(busanThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(gangnam1Thumb.getS3ObjectKey())).thenReturn(gangnam1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(null)).thenReturn(null);

        // when
        List<PlaceSimpleResponse> response = travelService.getPopularTravelPlacesByCity(CityType.ALL);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(busanPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(busanThumbUrl);
        assertThat(response.get(2).getPlaceName()).isEqualTo(gangnamPlace1.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(gangnam1ThumbUrl);
        assertThat(response.get(3).getPlaceName()).isEqualTo(gangnamPlace2.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("인기 여행지 조회 - 전라도")
    void findPopularTravelPlacesByCity_JEOLLA(){
        // given
        City jeolla1 = CityFixture.createCity(country, "전북특별자치도");
        District jeolla1District = DistrictFixture.createDistrict(jeolla1, "고창군");
        TravelPlace jeollaPlace1 = TravelPlaceFixture.createTravelPlace(
                country,
                jeolla1,
                jeolla1District,
                apiCategory,
                attractionContentType,
                "고창 여행지"
        );
        TravelImage jeolla1Thumb = TravelImageFixture.createTravelImage(jeollaPlace1, "부산이미지1", true);
        String jeolla1ThumbUrl = S3Fixture.createS3ObjectUrl(jeolla1Thumb.getS3ObjectKey());

        City jeolla2 = CityFixture.createCity(country, "전라남도");
        District jeolla2District = DistrictFixture.createDistrict(jeolla2, "보성구");
        TravelPlace jeollaPlace2 = TravelPlaceFixture.createTravelPlace(
                country,
                jeolla2,
                jeolla2District,
                apiCategory,
                attractionContentType,
                "보성 여행지"
        );

        List<PlaceSimpleQueryDto> mockResult = List.of(
                TravelPlaceFixture.createPlaceSimpleQueryDto(jeollaPlace2, null),
                TravelPlaceFixture.createPlaceSimpleQueryDto(jeollaPlace1, jeolla1Thumb.getS3ObjectKey())
        );

        when(travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA)).thenReturn(mockResult);
        when(s3ObjectManager.generateS3ObjectUrl(jeolla1Thumb.getS3ObjectKey())).thenReturn(jeolla1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(null)).thenReturn(null);

        // when
        List<PlaceSimpleResponse> response = travelService.getPopularTravelPlacesByCity(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(jeollaPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(jeollaPlace1.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(jeolla1ThumbUrl);
    }


    @Test
    @DisplayName("인기 여행지 조회 시 데이터 없는 경우")
    void findPopularTravelPlacesByCity_empty(){
        // given
        when(travelPlaceRepository.findPopularTravelPlaces(CityType.JEOLLA)).thenReturn(Collections.emptyList());

        // when
        List<PlaceSimpleResponse> response = travelService.getPopularTravelPlacesByCity(CityType.JEOLLA);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


    @Test
    @DisplayName("추천 테마 여행지 조회 - 전체")
    void findRecommendTravelPlacesByTheme_ALL(){
        // given
        TravelPlace attractionPlace1 = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage attraction1Thumb = TravelImageFixture.createTravelImage(attractionPlace1, "test1", true);
        TravelImageFixture.createTravelImage(attractionPlace1, "test2", false);
        String attraction1ThumbUrl = S3Fixture.createS3ObjectUrl(attraction1Thumb.getS3ObjectKey());

        TravelPlace lodgingPlace = TravelPlaceFixture.createLodgingTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                lodgingContentType,
                "여행지2"
        );

        City busan = CityFixture.createCity(country, "부산");
        District busanDistrict = DistrictFixture.createDistrict(busan, "금정구");
        TravelPlace sportsPlace = TravelPlaceFixture.createTravelPlace(
                country,
                busan,
                busanDistrict,
                apiCategory,
                sportsContentType,
                "부산 여행지"
        );
        TravelImage sportsThumb = TravelImageFixture.createTravelImage(sportsPlace, "부산이미지1", true);
        String sportsThumbUrl = S3Fixture.createS3ObjectUrl(sportsThumb.getS3ObjectKey());

        City jeolla = CityFixture.createCity(country, "전라남도");
        District jeollaDistrict = DistrictFixture.createDistrict(busan, "보성구");
        TravelPlace attractionPlace2 = TravelPlaceFixture.createTravelPlace(
                country,
                jeolla,
                jeollaDistrict,
                apiCategory,
                attractionContentType,
                "전라도 여행지"
        );

        List<PlaceSimpleQueryDto> mockResult = List.of(
                TravelPlaceFixture.createPlaceSimpleQueryDto(attractionPlace2, null),
                TravelPlaceFixture.createPlaceSimpleQueryDto(sportsPlace, sportsThumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceSimpleQueryDto(attractionPlace1, attraction1Thumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceSimpleQueryDto(lodgingPlace, null)
        );

        when(travelPlaceRepository.findRecommendTravelPlaces(ThemeType.All)).thenReturn(mockResult);
        when(s3ObjectManager.generateS3ObjectUrl(sportsThumb.getS3ObjectKey())).thenReturn(sportsThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(attraction1Thumb.getS3ObjectKey())).thenReturn(attraction1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(null)).thenReturn(null);

        // when
        List<PlaceSimpleResponse> response = travelService.getRecommendTravelPlacesByTheme(ThemeType.All);

        // then
        assertThat(response.size()).isEqualTo(4);
        assertThat(response.get(0).getPlaceName()).isEqualTo(attractionPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(sportsPlace.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(sportsThumbUrl);
        assertThat(response.get(2).getPlaceName()).isEqualTo(attractionPlace1.getPlaceName());
        assertThat(response.get(2).getThumbnailUrl()).isEqualTo(attraction1ThumbUrl);
        assertThat(response.get(3).getPlaceName()).isEqualTo(lodgingPlace.getPlaceName());
        assertThat(response.get(3).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("추천 테마 여행지 조회 - 관광지")
    void findRecommendTravelPlacesByTheme_ATTRACTIONS(){
        // given
        TravelPlace attractionPlace1 = TravelPlaceFixture.createTravelPlace(
                country,
                seoul,
                gangnam,
                apiCategory,
                attractionContentType,
                "여행지1"
        );
        TravelImage attraction1Thumb = TravelImageFixture.createTravelImage(attractionPlace1, "test1", true);
        TravelImageFixture.createTravelImage(attractionPlace1, "test2", false);
        String attraction1ThumbUrl = S3Fixture.createS3ObjectUrl(attraction1Thumb.getS3ObjectKey());

        City jeolla2 = CityFixture.createCity(country, "전라남도");
        District jeolla2District = DistrictFixture.createDistrict(jeolla2, "보성구");
        TravelPlace attractionPlace2 = TravelPlaceFixture.createTravelPlace(
                country,
                jeolla2,
                jeolla2District,
                apiCategory,
                attractionContentType,
                "전라도 여행지"
        );

        List<PlaceSimpleQueryDto> mockResult = List.of(
                TravelPlaceFixture.createPlaceSimpleQueryDto(attractionPlace2, null),
                TravelPlaceFixture.createPlaceSimpleQueryDto(attractionPlace1, attraction1Thumb.getS3ObjectKey())
        );

        when(travelPlaceRepository.findRecommendTravelPlaces(ThemeType.ATTRACTIONS)).thenReturn(mockResult);
        when(s3ObjectManager.generateS3ObjectUrl(attraction1Thumb.getS3ObjectKey())).thenReturn(attraction1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(null)).thenReturn(null);

        // when
        List<PlaceSimpleResponse> response = travelService.getRecommendTravelPlacesByTheme(ThemeType.ATTRACTIONS);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlaceName()).isEqualTo(attractionPlace2.getPlaceName());
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(1).getPlaceName()).isEqualTo(attractionPlace1.getPlaceName());
        assertThat(response.get(1).getThumbnailUrl()).isEqualTo(attraction1ThumbUrl);
    }


    @Test
    @DisplayName("추천 테마 여행지 조회 시 데이터 없는 경우")
    void findRecommendTravelPlacesByTheme_empty(){
        // given, when
        List<PlaceSimpleResponse> response = travelService.getRecommendTravelPlacesByTheme(ThemeType.FOOD);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response).isEmpty();

    }


}
