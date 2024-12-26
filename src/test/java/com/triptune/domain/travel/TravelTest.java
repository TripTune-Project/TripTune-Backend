package com.triptune.domain.travel;

import com.triptune.domain.BaseTest;
import com.triptune.domain.travel.dto.PlaceLocation;
import com.triptune.domain.travel.dto.request.PlaceLocationRequest;
import com.triptune.domain.travel.dto.request.PlaceSearchRequest;
import com.triptune.domain.travel.entity.TravelPlace;

public abstract class TravelTest extends BaseTest {
    protected PlaceLocationRequest createTravelLocationRequest(double latitude, double longitude){
        return PlaceLocationRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }


    protected PlaceSearchRequest createTravelSearchRequest(double longitude, double latitude, String keyword){
        return PlaceSearchRequest.builder()
                .longitude(longitude)
                .latitude(latitude)
                .keyword(keyword)
                .build();
    }

    protected PlaceLocation createTravelLocation(TravelPlace travelPlace){
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
                .distance(0.2345234234)
                .build();
    }

}
