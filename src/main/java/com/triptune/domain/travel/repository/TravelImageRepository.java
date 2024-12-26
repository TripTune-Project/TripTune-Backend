package com.triptune.domain.travel.repository;

import com.triptune.domain.travel.entity.TravelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TravelImageRepository extends JpaRepository<TravelImage, Long> {
    List<TravelImage> findByTravelPlacePlaceId(Long placeId);

    @Query(value = "SELECT t.s3ObjectUrl FROM TravelImage t WHERE t.travelPlace.placeId = :placeId AND t.isThumbnail = true")
    String findThumbnailUrlByPlaceId(@Param("placeId") Long placeId);
}
