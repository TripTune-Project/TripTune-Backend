package com.triptune.domain.travel.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelCustomRepository {
    Page<TravelPlace> findAllByAreaData(Pageable pageable, String country, String city, String district);
    Page<TravelPlace> findNearByTravelPlaceList(Pageable pageable, TravelLocationRequest travelLocationRequest, int distance);
    Integer getTotalElements(BooleanExpression booleanExpression);

}