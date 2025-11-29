package com.triptune.travel.entity;

import com.triptune.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_image_id")
    private Long travelImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private TravelPlace travelPlace;

    @Column(name = "s3_object_url")
    private String s3ObjectUrl;

    @Column(name = "s3_object_key")
    private String s3ObjectKey;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private double fileSize;

    @Column(name = "is_thumbnail")
    private boolean isThumbnail;


    @Builder
    public TravelImage(Long travelImageId, TravelPlace travelPlace, String s3ObjectUrl, String s3ObjectKey, String originalName, String fileName, String fileType, double fileSize, boolean isThumbnail) {
        this.travelImageId = travelImageId;
        this.travelPlace = travelPlace;
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3ObjectKey = s3ObjectKey;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.isThumbnail = isThumbnail;
    }
}
