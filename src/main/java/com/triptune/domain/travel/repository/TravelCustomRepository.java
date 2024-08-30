package com.triptune.domain.travel.repository;

import com.querydsl.core.types.Predicate;
import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelCustomRepository {
    Page<TravelPlace> findAllByAreaData(Pageable pageable, String country, String city, String district);
    Integer getTotalElements(Predicate predicate);

}
