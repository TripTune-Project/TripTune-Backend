package com.triptune.domain.travel.dto;

import com.triptune.domain.travel.entity.TravelImage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private List<TravelImage> travelImageList;
    private Double distance;

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
    public PlaceLocation(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, List<TravelImage> travelImageList, Double distance) {
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
        this.travelImageList = travelImageList;
        this.distance = distance;
    }


    public String getThumbnailUrl(){
        return travelImageList.stream()
                .filter(TravelImage::isThumbnail)
                .map(TravelImage::getS3ObjectUrl)
                .findFirst()
                .orElse(null);
    }
}
