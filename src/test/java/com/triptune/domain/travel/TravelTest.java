package com.triptune.domain.travel;

import com.triptune.domain.BaseTest;
import com.triptune.domain.travel.dto.TravelLocation;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.entity.TravelPlace;

public abstract class TravelTest extends BaseTest {
    protected TravelLocationRequest createTravelLocationRequest(double latitude, double longitude){
        return TravelLocationRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }


    protected TravelSearchRequest createTravelSearchRequest(double longitude, double latitude, String keyword){
        return TravelSearchRequest.builder()
                .longitude(longitude)
                .latitude(latitude)
                .keyword(keyword)
                .build();
    }

    protected TravelLocation createTravelLocation(TravelPlace place){
        return TravelLocation.builder()
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
