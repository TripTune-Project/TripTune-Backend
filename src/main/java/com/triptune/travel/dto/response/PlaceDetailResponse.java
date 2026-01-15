package com.triptune.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.triptune.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
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
    private double latitude;
    private double longitude;
    private String placeName;
    private boolean bookmarkStatus;
    private String description;
    private List<TravelImageResponse> imageList;

    @Builder
    public PlaceDetailResponse(Long placeId, String placeType, String country, String city, String district, String address, String detailAddress, String useTime, String checkInTime, String checkOutTime, String homepage, String phoneNumber, double latitude, double longitude, String placeName, boolean bookmarkStatus, String description, List<TravelImageResponse> imageList) {
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
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.bookmarkStatus = bookmarkStatus;
        this.description = description;
        this.imageList = imageList;
    }


    public static PlaceDetailResponse from(TravelPlace travelPlace, boolean bookmarkStatus){
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
                .latitude(travelPlace.getLatitude())
                .longitude(travelPlace.getLongitude())
                .placeName(travelPlace.getPlaceName())
                .bookmarkStatus(bookmarkStatus)
                .description(travelPlace.getDescription())
                .imageList(travelPlace.getTravelImages().stream()   // TravelImageFile -> TravelImageResponse 로 변경
                        .map(TravelImageResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }


}
