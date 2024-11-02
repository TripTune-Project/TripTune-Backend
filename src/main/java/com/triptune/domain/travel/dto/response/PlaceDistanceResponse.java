package com.triptune.domain.travel.dto.response;

import com.triptune.domain.travel.dto.PlaceLocation;
import com.triptune.domain.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
@Getter
@NoArgsConstructor
public class PlaceDistanceResponse {
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
    public PlaceDistanceResponse(Long placeId, String country, String city, String district, String address, String detailAddress, double longitude, double latitude, String placeName, String thumbnailUrl, Double distance) {
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

    public static PlaceDistanceResponse entityToDto(TravelPlace travelPlace){
        return PlaceDistanceResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .longitude(travelPlace.getLongitude())
                .latitude(travelPlace.getLatitude())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(travelPlace.getThumbnailUrl())
                .build();
    }

    public static List<PlaceDistanceResponse> entityListToDtoList(List<TravelPlace> travelPlaceList){
        return travelPlaceList.stream()
                .map(PlaceDistanceResponse::entityToDto)
                .collect(Collectors.toList());
    }


    public static PlaceDistanceResponse entityToLocationDto(PlaceLocation placeLocation){
        return PlaceDistanceResponse.builder()
                .placeId(placeLocation.getPlaceId())
                .country(placeLocation.getCountry())
                .city(placeLocation.getCity())
                .district(placeLocation.getDistrict())
                .address(placeLocation.getAddress())
                .detailAddress(placeLocation.getDetailAddress())
                .longitude(placeLocation.getLongitude())
                .latitude(placeLocation.getLatitude())
                .placeName(placeLocation.getPlaceName())
                .thumbnailUrl(placeLocation.getThumbnailUrl())
                .distance(placeLocation.getDistance())
                .build();
    }

}
