package com.triptune.travel.repository;

import com.triptune.travel.entity.TravelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelImageRepository extends JpaRepository<TravelImage, Long> {
}
