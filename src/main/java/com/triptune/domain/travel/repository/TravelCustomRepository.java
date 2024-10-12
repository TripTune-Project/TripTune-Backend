package com.triptune.domain.travel.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocation;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.dto.TravelSimpleResponse;
import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelCustomRepository {
    Page<TravelPlace> findAllByAreaData(Pageable pageable, String country, String city, String district);
    Page<TravelPlace> searchTravelPlaces(Pageable pageable, String keyword);
    Page<TravelLocation> findNearByTravelPlaces(Pageable pageable, TravelLocationRequest travelLocationRequest, int radius);
    Page<TravelLocation> searchTravelPlacesWithLocation(Pageable pageable, TravelSearchRequest travelSearchRequest);
    Integer getTotalElements(BooleanExpression booleanExpression);
}
