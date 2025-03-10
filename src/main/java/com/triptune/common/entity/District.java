package com.triptune.common.entity;

import com.triptune.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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


    @Builder
    public District(Long districtId, City city, String districtName) {
        this.districtId = districtId;
        this.city = city;
        this.districtName = districtName;
    }

    public void updateDistrictName(String districtName) {
        this.districtName = districtName;
    }
}
