package com.triptune.domain.travel.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.domain.travel.dto.PlaceLocationRequest;
import com.triptune.domain.travel.dto.PlaceLocation;
import com.triptune.domain.travel.dto.PlaceSearchRequest;
import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelPlaceCustomRepository {
    Page<TravelPlace> findAllByAreaData(Pageable pageable, String country, String city, String district);
    Page<TravelPlace> searchTravelPlaces(Pageable pageable, String keyword);
    Page<PlaceLocation> findNearByTravelPlaces(Pageable pageable, PlaceLocationRequest placeLocationRequest, int radius);
    Page<PlaceLocation> searchTravelPlacesWithLocation(Pageable pageable, PlaceSearchRequest placeSearchRequest);
    Integer getTotalElements(BooleanExpression booleanExpression);
}
