package com.triptune.domain.travel.dto.response;

import com.triptune.domain.travel.entity.TravelImage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TravelImageResponse {
    private Long fileId;
    private String imageName;
    private String imageUrl;

    @Builder
    public TravelImageResponse(Long fileId, String imageName, String imageUrl) {
        this.fileId = fileId;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
    }

    public static TravelImageResponse entityToDto(TravelImage travelImage){
        return TravelImageResponse.builder()
                .fileId(travelImage.getTravelImageId())
                .imageName(travelImage.getFileName())
                .imageUrl(travelImage.getS3ObjectUrl())
                .build();
    }
}
