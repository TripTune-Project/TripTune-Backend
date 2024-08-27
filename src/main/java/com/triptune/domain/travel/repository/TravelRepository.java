package com.triptune.domain.travel.repository;

import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelRepository {
    Optional<TravelPlace> findByDistrictId(int districtId);
}
