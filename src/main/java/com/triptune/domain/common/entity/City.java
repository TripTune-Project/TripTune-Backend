package com.triptune.domain.common.entity;

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
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private Long cityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @OneToMany(mappedBy = "city", orphanRemoval = true)
    private List<District> districtList;


    @Builder
    public City(Long cityId, Country country, List<District> districtList) {
        this.cityId = cityId;
        this.country = country;
        this.districtList = districtList;
    }
}
