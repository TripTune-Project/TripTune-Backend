package com.triptune.travel.repository.dto;

import com.triptune.travel.dto.response.PlaceResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceLocation {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private Double latitude;
    private Double longitude;
    private String placeName;
    private String thumbnailUrl;
    private Double distance;
    private boolean bookmarkStatus;

    @Builder
    public PlaceLocation(Long placeId, String country, String city, String district, String address, String detailAddress, Double latitude, Double longitude, String placeName, String thumbnailUrl, Double distance) {
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
        this.distance = distance;
    }

    public static PlaceLocation from(PlaceResponse placeResponse){
        return PlaceLocation.builder()
                .placeId(placeResponse.getPlaceId())
                .country(placeResponse.getCountry())
                .city(placeResponse.getCity())
                .district(placeResponse.getDistrict())
                .address(placeResponse.getAddress())
                .detailAddress(placeResponse.getDetailAddress())
                .latitude(placeResponse.getLatitude())
                .longitude(placeResponse.getLongitude())
                .placeName(placeResponse.getPlaceName())
                .thumbnailUrl(placeResponse.getThumbnailUrl())
                .build();
    }


    public void updateBookmarkStatusTrue(){
        this.bookmarkStatus = true;
    }
}
