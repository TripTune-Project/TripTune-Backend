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

    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelPlace> travelPlaceList;


    @Builder

    public District(Long districtId, City city, String districtName, List<TravelPlace> travelPlaceList) {
        this.districtId = districtId;
        this.city = city;
        this.districtName = districtName;
        this.travelPlaceList = travelPlaceList;
    }

    public void updateDistrictName(String districtName) {
        this.districtName = districtName;
    }
}
