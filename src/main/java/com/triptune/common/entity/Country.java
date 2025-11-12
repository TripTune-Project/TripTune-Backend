package com.triptune.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "country_name")
    private String countryName;


    @Builder
    public Country(Long countryId, String countryName) {
        this.countryId = countryId;
        this.countryName = countryName;
    }
}
