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

    protected PlaceLocation createTravelLocation(TravelPlace place){
        return PlaceLocation.builder()
                .placeId(1L)
                .country(place.getCountry().getCountryName())
                .city(place.getCity().getCityName())
                .district(place.getDistrict().getDistrictName())
                .address(place.getAddress())
                .detailAddress(place.getDetailAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .placeName(place.getPlaceName())
                .distance(0.2345234234)
                .build();
    }

}
