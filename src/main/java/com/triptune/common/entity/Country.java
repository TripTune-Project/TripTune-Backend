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
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Long CountryId;

    @Column(name = "country_name")
    private String countryName;


    @Builder
    public Country(Long countryId, String countryName) {
        CountryId = countryId;
        this.countryName = countryName;
    }
}
