package com.triptune.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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


    private District(City city, String districtName) {
        this.city = city;
        this.districtName = districtName;
    }

    public static District createDistrict(City city, String districtName) {
        return new District(city, districtName);
    }

}
