package com.triptune.domain.travel.dto;

import com.triptune.domain.travel.entity.TravelImage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TravelLocation {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private double longitude;
    private double latitude;
    private String placeName;
    private List<TravelImage> travelImageFileList;
    private Double distance;

    @Builder
    public TravelLocation(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, Double distance) {
        // travelImageFileList 미포함 생성자
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
    public TravelLocation(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, List<TravelImage> travelImageFileList, Double distance) {
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
        this.travelImageFileList = travelImageFileList;
        this.distance = distance;
    }
}
