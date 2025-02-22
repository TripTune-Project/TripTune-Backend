package com.triptune.travel.dto.response;

import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceDistanceResponse {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private double longitude;
    private double latitude;
    private String placeName;
    private String thumbnailUrl;
    private Double distance;
    private boolean bookmarkStatus;

    @Builder
    public PlaceDistanceResponse(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String thumbnailUrl, Double distance, boolean bookmarkStatus) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeName = placeName;
        this.thumbnailUrl = thumbnailUrl;
        this.distance = Math.floor(distance * 10) / 10.0;
        this.bookmarkStatus = bookmarkStatus;
    }

    public static PlaceDistanceResponse from(TravelPlace travelPlace){
        return PlaceDistanceResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .longitude(travelPlace.getLongitude())
                .latitude(travelPlace.getLatitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(travelPlace.getThumbnailUrl())
                .build();
    }

    public static PlaceDistanceResponse from(PlaceLocation placeLocation){
        return PlaceDistanceResponse.builder()
                .placeId(placeLocation.getPlaceId())
                .country(placeLocation.getCountry())
                .city(placeLocation.getCity())
                .district(placeLocation.getDistrict())
                .address(placeLocation.getAddress())
                .detailAddress(placeLocation.getDetailAddress())
                .longitude(placeLocation.getLongitude())
                .latitude(placeLocation.getLatitude())
                .placeName(placeLocation.getPlaceName())
                .thumbnailUrl(placeLocation.getThumbnailUrl())
                .distance(placeLocation.getDistance())
                .bookmarkStatus(placeLocation.isBookmarkStatus())
                .build();
    }


}
