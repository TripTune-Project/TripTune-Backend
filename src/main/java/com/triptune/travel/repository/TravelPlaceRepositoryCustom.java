package com.triptune.travel.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelPlaceRepositoryCustom {
    Page<PlaceResponse> findAllByAreaData(Pageable pageable, String country, String city, String district);
    Page<PlaceResponse> searchTravelPlaces(Pageable pageable, String keyword);
    Page<PlaceLocation> findNearByTravelPlaces(Pageable pageable, PlaceLocationRequest placeLocationRequest, int radius);
    Page<PlaceLocation> searchTravelPlacesWithLocation(Pageable pageable, PlaceSearchRequest placeSearchRequest);
    Integer countTotalElements(BooleanExpression booleanExpression);
}
