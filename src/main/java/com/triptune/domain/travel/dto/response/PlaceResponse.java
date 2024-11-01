package com.triptune.domain.travel.dto.response;

import com.triptune.domain.common.entity.File;
import com.triptune.domain.travel.entity.TravelPlace;
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
    private double longitude;
    private double latitude;
    private String placeName;
    private String thumbnailUrl;

    @Builder
    public PlaceResponse(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String thumbnailUrl) {
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
    }

    public void setThumbnailUrl(TravelPlace travelPlace) {
        this.thumbnailUrl = File.getThumbnailUrl(travelPlace.getTravelImageList());
    }

    public static PlaceResponse entityToDto(TravelPlace travelPlace){
        String thumbnailUrl = File.getThumbnailUrl(travelPlace.getTravelImageList());

        return PlaceResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .longitude(travelPlace.getLongitude())
                .latitude(travelPlace.getLatitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

}
