package com.triptune.travel.dto.response;

import com.triptune.bookmark.repository.dto.PlaceBookmarkQueryDto;
import com.triptune.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceBookmarkResponse {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private String placeName;
    private String thumbnailUrl;

    @Builder
    public PlaceBookmarkResponse(Long placeId, String country, String city, String district, String address, String detailAddress, String placeName, String thumbnailUrl) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.placeName = placeName;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static PlaceBookmarkResponse of(PlaceBookmarkQueryDto placeBookmark, String thumbnailUrl){
        return PlaceBookmarkResponse.builder()
                .placeId(placeBookmark.getPlaceId())
                .country(placeBookmark.getCountry())
                .city(placeBookmark.getCity())
                .district(placeBookmark.getDistrict())
                .address(placeBookmark.getAddress())
                .detailAddress(placeBookmark.getDetailAddress())
                .placeName(placeBookmark.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
