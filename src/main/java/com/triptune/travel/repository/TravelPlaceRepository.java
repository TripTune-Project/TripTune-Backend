package com.triptune.travel.repository;

import com.triptune.travel.entity.TravelPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPlaceRepository extends JpaRepository<TravelPlace, Long>, TravelPlaceRepositoryCustom {
}
