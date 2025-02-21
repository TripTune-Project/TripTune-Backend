package com.triptune.domain.travel.entity;

import com.triptune.domain.bookmark.entity.Bookmark;
import com.triptune.domain.common.entity.*;
import com.triptune.domain.schedule.entity.TravelRoute;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "travelPlace", fetch = FetchType.LAZY)
    private List<TravelImage> travelImageList = new ArrayList<>();

    @OneToMany(mappedBy = "travelPlace", fetch = FetchType.LAZY)
    private List<TravelRoute> travelRouteList = new ArrayList<>();

    @OneToMany(mappedBy = "travelPlace", fetch = FetchType.LAZY)
    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Builder
    public TravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, String address, String detailAddress, String useTime, String checkInTime, String checkOutTime, String homepage, String phoneNumber, double longitude, double latitude, String description, int bookmarkCnt, LocalDateTime createdAt, LocalDateTime updatedAt, List<TravelImage> travelImageList, List<TravelRoute> travelRouteList, List<Bookmark> bookmarkList) {
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.travelImageList = travelImageList;
        this.travelRouteList = travelRouteList;
        this.bookmarkList = bookmarkList;
    }

    public String getThumbnailUrl(){
        return travelImageList.stream()
                .filter(TravelImage::isThumbnail)
                .map(TravelImage::getS3ObjectUrl)
                .findFirst()
                .orElse(null);
    }

    public void updateBookmarkCnt() {
        this.bookmarkCnt++;
    }
}
