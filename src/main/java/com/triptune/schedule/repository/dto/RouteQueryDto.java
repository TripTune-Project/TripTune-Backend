package com.triptune.schedule.repository.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteQueryDto {
    private int routeOrder;
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private Double latitude;
    private Double longitude;
    private String placeName;
    private String thumbnailS3ObjectKey;

    @Builder
    public RouteQueryDto(int routeOrder, Long placeId, String country, String city, String district, String address, String detailAddress, Double latitude, Double longitude, String placeName, String thumbnailS3ObjectKey) {
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
        this.thumbnailS3ObjectKey = thumbnailS3ObjectKey;
    }
}

