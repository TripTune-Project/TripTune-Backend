package com.triptune.travel.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TravelPlaceRepositoryCustom {
    Page<PlaceLocation> findNearByTravelPlaces(Pageable pageable, PlaceLocationRequest placeLocationRequest, int radius);
    Page<PlaceLocation> searchTravelPlacesWithLocation(Pageable pageable, PlaceSearchRequest placeSearchRequest);
    Page<PlaceResponse> searchTravelPlaces(Pageable pageable, String keyword);
    Integer countTotalElements(BooleanExpression booleanExpression);
    List<PlaceSimpleResponse> findPopularTravelPlaces(CityType cityType);
    List<PlaceSimpleResponse> findRecommendTravelPlaces(ThemeType themeType);
    Page<PlaceResponse> findDefaultTravelPlacesByJungGu(Pageable pageable);
}
