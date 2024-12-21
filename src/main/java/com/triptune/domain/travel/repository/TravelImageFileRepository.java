package com.triptune.domain.travel.repository;

import com.triptune.domain.travel.entity.TravelImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelImageFileRepository extends JpaRepository<TravelImageFile, Long> {
    List<TravelImageFile> findByTravelPlacePlaceId(Long placeId);
}
