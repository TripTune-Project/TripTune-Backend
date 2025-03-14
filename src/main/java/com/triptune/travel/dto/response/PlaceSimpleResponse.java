package com.triptune.travel.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceSimpleResponse {
    private Long placeId;
    private String address;
    private String detailAddress;
    private String placeName;
    private String thumbnailUrl;

    @Builder
    public PlaceSimpleResponse(Long placeId, String address, String detailAddress, String placeName, String thumbnailUrl) {
        this.placeId = placeId;
        this.address = address;
        this.detailAddress = detailAddress;
        this.placeName = placeName;
        this.thumbnailUrl = thumbnailUrl;
    }

}
