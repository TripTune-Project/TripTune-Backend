package com.triptune.bookmark.repository.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceBookmarkQueryDto {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private String placeName;
    private String thumbnailS3ObjectKey;

    @Builder
    public PlaceBookmarkQueryDto(Long placeId, String country, String city, String district, String address, String detailAddress, String placeName, String thumbnailS3ObjectKey) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.placeName = placeName;
        this.thumbnailS3ObjectKey = thumbnailS3ObjectKey;
    }

}
