package com.triptune.domain.travel.entity;

import com.triptune.domain.common.entity.Category;
import com.triptune.domain.common.entity.City;
import com.triptune.domain.common.entity.Country;
import com.triptune.domain.common.entity.District;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TravelPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private Category category;

    @Column(name = "content_type_id")
    private Long contentTypeId;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "api_content_id")
    private int apiContentId;

    @Column(name = "description")
    private String description;

    @Column(name = "bookmark_cnt")
    private int bookmarkCnt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "api_created_at")
    private LocalDateTime apiCreatedAt;

    @Column(name = "api_updated_at")
    private LocalDateTime apiUpdatedAt;

    @OneToMany(mappedBy = "travelPlace", orphanRemoval = true)
    private List<TravelImageFile> travelImageFileList;


    @Builder
    public TravelPlace(Long placeId, Country country, City city, District district, Category category, Long contentTypeId, String placeName, String address, String detailAddress, double longitude, double latitude, int apiContentId, String description, int bookmarkCnt, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime apiCreatedAt, LocalDateTime apiUpdatedAt, List<TravelImageFile> travelImageFileList) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.category = category;
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
        this.travelImageFileList = travelImageFileList;
    }

}
