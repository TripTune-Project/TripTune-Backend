package com.triptune.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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


    private City(Country country, String cityName) {
        this.country = country;
        this.cityName = cityName;
    }

    public static City createCity(Country country, String cityName) {
        return new City(country, cityName);
    }
}
