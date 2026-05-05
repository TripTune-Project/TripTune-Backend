package com.triptune.travel.dto.response;

import com.triptune.travel.repository.dto.PlaceDistanceQueryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceDistanceResponse {
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
    private Double distance;
    private boolean bookmarkStatus;

    @Builder
    public PlaceDistanceResponse(Long placeId, String country, String city, String district, String address, String detailAddress, Double latitude, Double longitude, String placeName, String thumbnailUrl, Double distance, boolean bookmarkStatus) {
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
        this.distance = distance;
        this.bookmarkStatus = bookmarkStatus;
    }


    public static PlaceDistanceResponse of(PlaceDistanceQueryDto placeDistance, String thumbnailUrl){
        return PlaceDistanceResponse.builder()
                .placeId(placeDistance.getPlaceId())
                .country(placeDistance.getCountry())
                .city(placeDistance.getCity())
                .district(placeDistance.getDistrict())
                .address(placeDistance.getAddress())
                .detailAddress(placeDistance.getDetailAddress())
                .latitude(placeDistance.getLatitude())
                .longitude(placeDistance.getLongitude())
                .placeName(placeDistance.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .distance(placeDistance.getDistance())
                .bookmarkStatus(false)
                .build();
    }

    public void updateBookmarkStatusTrue(){
        this.bookmarkStatus = true;
    }

}
