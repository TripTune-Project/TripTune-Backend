package com.triptune.domain.travel.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TravelPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "content_type_id")
    private Long contentTypeId;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "api_content_id")
    private Integer apiContentId;

    @Column(name = "description")
    private String description;

    @Column(name = "bookmark_cnt")
    private Integer bookmarkCnt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "api_created_at")
    private LocalDateTime apiCreatedAt;

    @Column(name = "api_updated_at")
    private LocalDateTime apiUpdatedAt;

    @Builder
    public TravelPlace(Long placeId, Long districtId, String categoryCode, Long contentTypeId, String placeName, String address, String detailAddress, Double longitude, Double latitude, Integer apiContentId, String description, Integer bookmarkCnt, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime apiCreatedAt, LocalDateTime apiUpdatedAt) {
        this.placeId = placeId;
        this.districtId = districtId;
        this.categoryCode = categoryCode;
        this.contentTypeId = contentTypeId;
        this.placeName = placeName;
        this.address = address;
        this.detailAddress = detailAddress;
        this.longitude = longitude;
        this.latitude = latitude;
        this.apiContentId = apiContentId;
        this.description = description;
        this.bookmarkCnt = bookmarkCnt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.apiCreatedAt = apiCreatedAt;
        this.apiUpdatedAt = apiUpdatedAt;
    }
}
