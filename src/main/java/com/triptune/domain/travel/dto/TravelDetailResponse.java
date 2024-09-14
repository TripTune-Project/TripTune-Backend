package com.triptune.domain.travel.dto;

import com.triptune.domain.common.entity.File;
import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TravelDetailResponse {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private double longitude;
    private double latitude;
    private String placeName;
    private String description;
    private List<TravelImageResponse> imageList;

    @Builder
    public TravelDetailResponse(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String description, List<TravelImageResponse> imageList) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeName = placeName;
        this.description = description;
        this.imageList = imageList;
    }


    public static TravelDetailResponse entityToDto(TravelPlace travelPlace){
        return TravelDetailResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .longitude(travelPlace.getLongitude())
                .latitude(travelPlace.getLatitude())
                .placeName(travelPlace.getPlaceName())
                .description(travelPlace.getDescription())
                .imageList(travelPlace.getTravelImageFileList().stream()   // TravelImageFile -> TravelImageResponse 로 변경
                        .map(TravelImageResponse::entityToDto)
                        .collect(Collectors.toList()))
                .build();
    }


}
