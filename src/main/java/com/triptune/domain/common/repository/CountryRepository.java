package com.triptune.domain.common.repository;

import com.triptune.domain.common.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
}
