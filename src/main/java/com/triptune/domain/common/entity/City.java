package com.triptune.domain.common.entity;

import com.triptune.domain.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private Long cityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(name = "city_name")
    private String cityName;

    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private List<District> districtList;

    @OneToMany(mappedBy = "city",fetch = FetchType.LAZY)
    private List<TravelPlace> travelPlaceList;


    @Builder
    public City(Long cityId, Country country, String cityName, List<District> districtList, List<TravelPlace> travelPlaceList) {
        this.cityId = cityId;
        this.country = country;
        this.cityName = cityName;
        this.districtList = districtList;
        this.travelPlaceList = travelPlaceList;
    }
}
