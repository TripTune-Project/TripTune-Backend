package com.triptune.domain.travel.repository;

import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPlaceRepository extends JpaRepository<TravelPlace, Long>, TravelPlaceCustomRepository {
    Optional<TravelPlace> findByPlaceId(Long placeId);
}
