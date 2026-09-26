package com.triptune.travel.fixture;

import com.triptune.common.entity.*;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.*;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.dto.PlaceDistanceQueryDto;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import com.triptune.travel.repository.dto.PlaceSimpleQueryDto;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class TravelPlaceFixture {
    
    // 숙박(checkInTime, checkOutTime not null / userTime null)
    public static TravelPlace createLodgingTravelPlace(Country country, City city, District district, ApiContentType apiContentType, String placeName){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                null,
                "15:00",
                "11:00",
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + "상세설명",
                0
        );
    }

    // 숙박 외(checkInTime, checkOutTime null / userTime not null)
    public static TravelPlace createTravelPlace(Country country, City city, District district, ApiContentType apiContentType, String placeName){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + "상세설명",
                0
        );
    }

    public static TravelPlace createTravelPlaceWithId(Long placeId, Country country, City city, District district, ApiContentType apiContentType, String placeName){
        TravelPlace travelPlace = TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + "상세설명",
                0
        );

        ReflectionTestUtils.setField(travelPlace, "placeId", placeId);
        return travelPlace;
    }

    // 위도, 경도 지정
    public static TravelPlace createTravelPlaceWithLocation(Country country, City city, District district, ApiContentType apiContentType, String placeName, double latitude, double longitude){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                latitude,
                longitude,
                placeName+ " 상세설명",
                0
        );
    }


    public static TravelPlace createTravelPlaceWithIdAndLocation(Long placeId, Country country, City city, District district, ApiContentType apiContentType, String placeName, double latitude, double longitude){
        TravelPlace travelPlace = TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                latitude,
                longitude,
                placeName+ " 상세설명",
                0
        );

        ReflectionTestUtils.setField(travelPlace, "placeId", placeId);
        return travelPlace;
    }


    // 북마크 횟수 지정
    public static TravelPlace createTravelPlaceWithBookmarkCnt(Country country, City city, District district, ApiContentType apiContentType, String placeName, int bookmarkCnt){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + " 상세설명",
                bookmarkCnt
        );
    }

    public static PlaceLocationRequest createTravelLocationRequest(Double latitude, Double longitude){
        return PlaceLocationRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }


    public static PlaceSearchRequest createTravelSearchRequest(Double latitude, Double longitude, String keyword){
        return PlaceSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .keyword(keyword)
                .build();
    }

    public static PlaceSearchRequest createTravelSearchRequest(String keyword){
        return PlaceSearchRequest.builder()
                .keyword(keyword)
                .build();
    }


    public static PlaceDistanceQueryDto createPlaceDistanceQueryDto(TravelPlace travelPlace, String thumbnailS3ObjectKey){
        return PlaceDistanceQueryDto.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .latitude(travelPlace.getLatitude())
                .longitude(travelPlace.getLongitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .distance(0.2345234234)
                .build();
    }


    public static PlaceQueryDto createPlaceQueryDto(TravelPlace travelPlace, String thumbnailS3ObjectKey){
        return PlaceQueryDto.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .latitude(travelPlace.getLatitude())
                .longitude(travelPlace.getLongitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .build();
    }

    public static PlaceSimpleQueryDto createPlaceSimpleQueryDto(TravelPlace travelPlace, String thumbnailS3ObjectKey){
        return PlaceSimpleQueryDto.builder()
                .placeId(travelPlace.getPlaceId())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .placeName(travelPlace.getPlaceName())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .build();
    }


    public static PlaceResponse createPlaceResponse(TravelPlace place, String thumbnailUrl) {
        return PlaceResponse.builder()
                .placeId(place.getPlaceId())
                .country(place.getCountry().getCountryName())
                .city(place.getCity().getCityName())
                .district(place.getDistrict().getDistrictName())
                .detailAddress(place.getDetailAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .placeName(place.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public static PlaceDistanceResponse createPlaceDistanceResponse(TravelPlace place, String thumbnailUrl, Double distance, boolean bookmarkStatus) {
        return PlaceDistanceResponse.builder()
                .placeId(place.getPlaceId())
                .country(place.getCountry().getCountryName())
                .city(place.getCity().getCityName())
                .district(place.getDistrict().getDistrictName())
                .address(place.getAddress())
                .detailAddress(place.getDetailAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .placeName(place.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .distance(distance)
                .bookmarkStatus(bookmarkStatus)
                .build();

    }

    public static PlaceDetailResponse createPlaceDetailResponse(TravelPlace place, List<TravelImageResponse> images, boolean bookmarkStatus) {
        return PlaceDetailResponse.of(place, images, bookmarkStatus);
    }

    public static PlaceSimpleResponse createPlaceSimpleResponse(TravelPlace place, String thumbnailUrl) {
        return PlaceSimpleResponse.builder()
                .placeId(place.getPlaceId())
                .address(place.getAddress())
                .detailAddress(place.getDetailAddress())
                .placeName(place.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();

    }
}
