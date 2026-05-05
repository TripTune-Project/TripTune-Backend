package com.triptune.travel.repository.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceSimpleQueryDto {
    private Long placeId;
    private String address;
    private String detailAddress;
    private String placeName;
    private String thumbnailS3ObjectKey;

    @Builder
    public PlaceSimpleQueryDto(Long placeId, String address, String detailAddress, String placeName, String thumbnailS3ObjectKey) {
        this.placeId = placeId;
        this.address = address;
        this.detailAddress = detailAddress;
        this.placeName = placeName;
        this.thumbnailS3ObjectKey = thumbnailS3ObjectKey;
    }

}
