package com.triptune.domain.travel.dto;

import com.triptune.domain.common.entity.File;
import com.triptune.domain.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
@Getter
@NoArgsConstructor
public class TravelResponse {
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
    private Double distance;

    @Builder
    public TravelResponse(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String thumbnailUrl, Double distance) {
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
        this.distance = Math.floor(distance * 10) / 10.0;
    }

    public void setThumbnailUrl(TravelPlace travelPlace) {
        this.thumbnailUrl = File.getThumbnailUrl(travelPlace.getTravelImageList());
    }

    public static TravelResponse entityToDto(TravelPlace travelPlace){
        String thumbnailUrl = File.getThumbnailUrl(travelPlace.getTravelImageList());

        return TravelResponse.builder()
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

    public static List<TravelResponse> entityListToDtoList(List<TravelPlace> travelPlaceList){
        return travelPlaceList.stream()
                .map(TravelResponse::entityToDto)
                .collect(Collectors.toList());
    }


    public static TravelResponse entityToLocationDto(TravelLocationResponse travelLocationResponse){
        String thumbnailUrl = File.getThumbnailUrl(travelLocationResponse.getTravelImageFileList());

        return TravelResponse.builder()
                .placeId(travelLocationResponse.getPlaceId())
                .country(travelLocationResponse.getCountry())
                .city(travelLocationResponse.getCity())
                .district(travelLocationResponse.getDistrict())
                .address(travelLocationResponse.getAddress())
                .detailAddress(travelLocationResponse.getDetailAddress())
                .longitude(travelLocationResponse.getLongitude())
                .latitude(travelLocationResponse.getLatitude())
                .placeName(travelLocationResponse.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .distance(travelLocationResponse.getDistance())
                .build();
    }

}
