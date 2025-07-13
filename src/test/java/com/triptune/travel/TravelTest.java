package com.triptune.travel;

import com.triptune.BaseTest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelPlace;

public abstract class TravelTest extends BaseTest {
    protected PlaceLocationRequest createTravelLocationRequest(Double latitude, Double longitude){
        return PlaceLocationRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }


    protected PlaceSearchRequest createTravelSearchRequest(Double longitude, Double latitude, String keyword){
        return PlaceSearchRequest.builder()
                .longitude(longitude)
                .latitude(latitude)
                .keyword(keyword)
                .build();
    }

    protected PlaceSearchRequest createTravelSearchRequest(String keyword){
        return PlaceSearchRequest.builder()
                .keyword(keyword)
                .build();
    }

    protected PlaceLocation createPlaceLocation(TravelPlace travelPlace, String thumbnailUrl){
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

    protected PlaceResponse createPlaceResponse(TravelPlace travelPlace, String thumbnailUrl){
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


    protected PlaceSimpleResponse createPlaceSimpleResponse(TravelPlace travelPlace, String thumbnailUrl){
        return PlaceSimpleResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

}
