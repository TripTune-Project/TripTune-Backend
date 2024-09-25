package com.triptune.domain.travel.repository;

import com.triptune.domain.travel.entity.TravelImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelImageRepository extends JpaRepository<TravelImage, Long> {
    List<TravelImage> findByTravelPlacePlaceId(Long placeId);

}
