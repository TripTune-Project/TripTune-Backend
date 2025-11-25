package com.triptune.travel.entity;

import com.triptune.common.entity.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelPlace extends BaseTimeEntity {

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
    private ApiCategory apiCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_type_id")
    private ApiContentType apiContentType;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "use_time")
    private String useTime;

    @Column(name = "check_in_time")
    private String checkInTime;

    @Column(name = "check_out_time")
    private String checkOutTime;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "description")
    private String description;

    @Column(name = "bookmark_cnt")
    private int bookmarkCnt;

    @OneToMany(mappedBy = "travelPlace", fetch = FetchType.LAZY)
    private List<TravelImage> travelImages = new ArrayList<>();


    @Builder
    public TravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, String address, String detailAddress, String useTime, String checkInTime, String checkOutTime, String homepage, String phoneNumber, double longitude, double latitude, String description, int bookmarkCnt, List<TravelImage> travelImages) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.apiCategory = apiCategory;
        this.apiContentType = apiContentType;
        this.placeName = placeName;
        this.address = address;
        this.detailAddress = detailAddress;
        this.useTime = useTime;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.homepage = homepage;
        this.phoneNumber = phoneNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description = description;
        this.bookmarkCnt = bookmarkCnt;
        this.travelImages = travelImages;
    }

    public String getThumbnailUrl(){
        return travelImages.stream()
                .filter(TravelImage::isThumbnail)
                .map(TravelImage::getS3ObjectUrl)
                .findFirst()
                .orElse(null);
    }

    public void increaseBookmarkCnt() {
        this.bookmarkCnt++;
    }

    public void decreaseBookmarkCnt(){
        this.bookmarkCnt--;
    }
}
