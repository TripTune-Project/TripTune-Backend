package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.repository.dto.RouteQueryDto;
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


    public static RouteResponse of(RouteQueryDto routeQueryDto, String thumbnailUrl){
        return RouteResponse.builder()
                .routeOrder(routeQueryDto.getRouteOrder())
                .placeId(routeQueryDto.getPlaceId())
                .country(routeQueryDto.getCountry())
                .city(routeQueryDto.getCity())
                .district(routeQueryDto.getDistrict())
                .address(routeQueryDto.getAddress())
                .detailAddress(routeQueryDto.getDetailAddress())
                .latitude(routeQueryDto.getLatitude())
                .longitude(routeQueryDto.getLongitude())
                .placeName(routeQueryDto.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
