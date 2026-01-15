package com.triptune.travel.entity;

import com.triptune.common.entity.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(name = "latitude")
    private double latitude;    // 위도

    @Column(name = "longitude")
    private double longitude;   // 경도

    @Column(name = "description")
    private String description;

    @Column(name = "bookmark_cnt")
    private int bookmarkCnt;

    @OneToMany(mappedBy = "travelPlace", fetch = FetchType.LAZY)
    private List<TravelImage> travelImages = new ArrayList<>();

    private TravelPlace(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, String address, String detailAddress, String useTime, String checkInTime, String checkOutTime, String homepage, String phoneNumber, double latitude, double longitude, String description, int bookmarkCnt) {
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
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.bookmarkCnt = bookmarkCnt;
    }


    public static TravelPlace createTravelPlace(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, String address, String detailAddress, String useTime, String checkInTime, String checkOutTime, String homepage, String phoneNumber, double latitude, double longitude, String description, int bookmarkCnt) {
        return new TravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                address,
                detailAddress,
                useTime,
                checkInTime,
                checkOutTime,
                homepage,
                phoneNumber,
                latitude,
                longitude,
                description,
                bookmarkCnt
        );

    }

    protected void addTravelImages(TravelImage travelImage){
        travelImages.add(travelImage);
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
