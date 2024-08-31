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
@Setter
@NoArgsConstructor
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long districtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "api_sigungu_code")
    private Integer apiSigunguCode;

    @OneToMany(mappedBy = "district", orphanRemoval = true)
    private List<TravelPlace> travelPlaceList;


    @Builder

    public District(Long districtId, City city, String districtName, Integer apiSigunguCode, List<TravelPlace> travelPlaceList) {
        this.districtId = districtId;
        this.city = city;
        this.districtName = districtName;
        this.apiSigunguCode = apiSigunguCode;
        this.travelPlaceList = travelPlaceList;
    }
}