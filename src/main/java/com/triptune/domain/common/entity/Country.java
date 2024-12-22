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
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Long CountryId;

    @Column(name = "country_name")
    private String countryName;

    // orphanRemoval = true : 부모 엔티티 제거 시 자식 엔티티도 제거됨
    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<City> cityList;

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelPlace> travelPlaceList;

    @Builder
    public Country(Long countryId, String countryName, List<City> cityList, List<TravelPlace> travelPlaceList) {
        CountryId = countryId;
        this.countryName = countryName;
        this.cityList = cityList;
        this.travelPlaceList = travelPlaceList;
    }
}
