package com.triptune.domain.travel.repository;

import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelRepository extends JpaRepository<TravelPlace, Long> {
    Page<TravelPlace> findAllByCountryCountryNameAndCityCityNameAndDistrictDistrictName(Pageable pageable, String countryName, String cityName, String districtName);
}
