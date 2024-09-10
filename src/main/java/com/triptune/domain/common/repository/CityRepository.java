package com.triptune.domain.common.repository;

import com.triptune.domain.common.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}
