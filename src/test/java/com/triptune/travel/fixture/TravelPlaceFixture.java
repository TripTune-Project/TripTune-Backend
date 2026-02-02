package com.triptune.travel.fixture;


import com.triptune.common.entity.*;
import com.triptune.travel.repository.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelPlace;
import org.springframework.test.util.ReflectionTestUtils;

public class TravelPlaceFixture {
    
    // 숙박(checkInTime, checkOutTime not null / userTime null)
    public static TravelPlace createLodgingTravelPlace(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
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
    public static TravelPlace createTravelPlace(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
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

    public static TravelPlace createTravelPlaceWithId(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName){
        TravelPlace travelPlace = TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
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
    public static TravelPlace createTravelPlaceWithLocation(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, double latitude, double longitude){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
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


    public static TravelPlace createTravelPlaceWithIdAndLocation(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, double latitude, double longitude){
        TravelPlace travelPlace = TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
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
    public static TravelPlace createTravelPlaceWithBookmarkCnt(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, int bookmarkCnt){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
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

    public static PlaceLocation createPlaceLocation(TravelPlace travelPlace, String thumbnailUrl){
        return PlaceLocation.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .latitude(travelPlace.getLatitude())
                .longitude(travelPlace.getLongitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .distance(0.2345234234)
                .build();
    }

    public static PlaceResponse createPlaceResponse(TravelPlace travelPlace, String thumbnailUrl){
        return PlaceResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .latitude(travelPlace.getLatitude())
                .longitude(travelPlace.getLongitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }


    public static PlaceSimpleResponse createPlaceSimpleResponse(TravelPlace travelPlace, String thumbnailUrl){
        return PlaceSimpleResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

}
