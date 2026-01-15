package com.triptune.travel.dto.response;

import com.triptune.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceResponse {
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
    public PlaceResponse(Long placeId, String country, String city, String district, String address, String detailAddress, double latitude, double longitude, String placeName, String thumbnailUrl) {
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

    public static PlaceResponse from(TravelPlace travelPlace){
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
                .thumbnailUrl(travelPlace.getThumbnailUrl())
                .build();
    }


}
