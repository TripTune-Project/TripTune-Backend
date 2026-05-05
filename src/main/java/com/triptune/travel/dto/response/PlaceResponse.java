package com.triptune.travel.dto.response;

import com.triptune.travel.repository.dto.PlaceQueryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceResponse {
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


    @Builder
    public PlaceResponse(Long placeId, String country, String city, String district, String address, String detailAddress, Double latitude, Double longitude, String placeName, String thumbnailUrl) {
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
    }

    public static PlaceResponse of(PlaceQueryDto placeQueryDto, String thumbnailUrl){
        return PlaceResponse.builder()
                .placeId(placeQueryDto.getPlaceId())
                .country(placeQueryDto.getCountry())
                .city(placeQueryDto.getCity())
                .district(placeQueryDto.getDistrict())
                .address(placeQueryDto.getAddress())
                .detailAddress(placeQueryDto.getDetailAddress())
                .latitude(placeQueryDto.getLatitude())
                .longitude(placeQueryDto.getLongitude())
                .placeName(placeQueryDto.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }



}
