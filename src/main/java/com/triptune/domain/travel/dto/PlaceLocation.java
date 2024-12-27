package com.triptune.domain.travel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
    public PlaceLocation(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, Double distance) {
        // travelImageFileList 포함되지 않은 생성자 -> TravelPlaceCustomRepositoryImpl 에서 사용
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeName = placeName;
        this.distance = distance;
    }

    @Builder
    public PlaceLocation(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String thumbnailUrl, Double distance, boolean bookmarkStatus) {
        // travelImageFileList 포함 생성자
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

    public void updateThumbnailUrl(String thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateBookmarkStatusTrue(){
        this.bookmarkStatus = true;
    }
}
