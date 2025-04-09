package com.triptune.travel.dto;

import com.triptune.travel.dto.response.PlaceResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class PlaceLocation {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private Double longitude;
    private Double latitude;
    private String placeName;
    private String thumbnailUrl;
    private Double distance;
    private boolean bookmarkStatus;

    @Builder
    public PlaceLocation(Long placeId, String country, String city, String district, String address, String detailAddress, Double longitude, Double latitude, String placeName, String thumbnailUrl, Double distance) {
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
        this.distance = distance;
    }

    @Builder
    public PlaceLocation(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String thumbnailUrl, Double distance, boolean bookmarkStatus) {
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
        this.distance = distance;
        this.bookmarkStatus = bookmarkStatus;
    }

    public static PlaceLocation from(PlaceResponse placeResponse){
        return PlaceLocation.builder()
                .placeId(placeResponse.getPlaceId())
                .country(placeResponse.getCountry())
                .city(placeResponse.getCity())
                .district(placeResponse.getDistrict())
                .address(placeResponse.getAddress())
                .detailAddress(placeResponse.getDetailAddress())
                .longitude(placeResponse.getLongitude())
                .latitude(placeResponse.getLatitude())
                .placeName(placeResponse.getPlaceName())
                .thumbnailUrl(placeResponse.getThumbnailUrl())
                .build();
    }


    public void updateBookmarkStatusTrue(){
        this.bookmarkStatus = true;
    }
}
