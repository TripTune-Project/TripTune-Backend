package com.triptune.domain.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.triptune.domain.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceDetailResponse {
    private Long placeId;
    private String placeType;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private String useTime;
    private String checkInTime;
    private String checkOutTime;
    private String homepage;
    private String phoneNumber;
    private double longitude;
    private double latitude;
    private String placeName;
    private String description;
    private List<TravelImageResponse> imageList;

    @Builder
    public PlaceDetailResponse(Long placeId, String placeType, String country, String city, String district, String address, String detailAddress, String useTime, String checkInTime, String checkOutTime, String homepage, String phoneNumber, double longitude, double latitude, String placeName, String description, List<TravelImageResponse> imageList) {
        this.placeId = placeId;
        this.placeType = placeType;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.useTime = useTime;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.homepage = homepage;
        this.phoneNumber = phoneNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeName = placeName;
        this.description = description;
        this.imageList = imageList;
    }


    public static PlaceDetailResponse from(TravelPlace travelPlace){
        return PlaceDetailResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .placeType(travelPlace.getApiContentType().getContentTypeName())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .useTime(travelPlace.getUseTime())
                .checkInTime(travelPlace.getCheckInTime())
                .checkOutTime(travelPlace.getCheckOutTime())
                .homepage(travelPlace.getHomepage())
                .phoneNumber(travelPlace.getPhoneNumber())
                .longitude(travelPlace.getLongitude())
                .latitude(travelPlace.getLatitude())
                .placeName(travelPlace.getPlaceName())
                .description(travelPlace.getDescription())
                .imageList(travelPlace.getTravelImageList().stream()   // TravelImageFile -> TravelImageResponse 로 변경
                        .map(TravelImageResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }


}
