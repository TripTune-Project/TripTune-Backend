package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelRoute;
import com.triptune.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteResponse {
    private int routeOrder;
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private double latitude;
    private double longitude;
    private String placeName;
    private String thumbnailUrl;

    @Builder
    public RouteResponse(int routeOrder, Long placeId, String country, String city, String district, String address, String detailAddress, double latitude, double longitude, String placeName, String thumbnailUrl) {
        this.routeOrder = routeOrder;
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.thumbnailUrl = thumbnailUrl;
    }


    public static RouteResponse from(TravelRoute travelRoute){
        TravelPlace travelPlace = travelRoute.getTravelPlace();

        return RouteResponse.builder()
                .routeOrder(travelRoute.getRouteOrder())
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .latitude(travelPlace.getLatitude())
                .longitude(travelPlace.getLongitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(travelPlace.getThumbnailUrl())
                .build();
    }
}
