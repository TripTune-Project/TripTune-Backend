package com.triptune.travel.repository;

import com.triptune.travel.entity.TravelPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelPlaceRepository extends JpaRepository<TravelPlace, Long>, TravelPlaceRepositoryCustom {
}
