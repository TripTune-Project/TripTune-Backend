package com.triptune.travel.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.dto.PlaceDistanceQueryDto;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import com.triptune.travel.repository.dto.PlaceSimpleQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TravelPlaceRepositoryCustom {
    Page<PlaceDistanceQueryDto> findNearByTravelPlaces(Pageable pageable, PlaceLocationRequest placeLocationRequest, int radius);
    Page<PlaceDistanceQueryDto> searchTravelPlacesWithLocation(Pageable pageable, PlaceSearchRequest placeSearchRequest);
    Page<PlaceDistanceQueryDto> searchTravelPlacesWithoutLocation(Pageable pageable, String keyword);
    Page<PlaceQueryDto> searchTravelPlaces(Pageable pageable, String keyword);
    Integer countTotalElements(BooleanExpression booleanExpression);
    Page<PlaceQueryDto> findNearbyTravelPlacesFromJungGu(Pageable pageable);
    List<PlaceSimpleQueryDto> findPopularTravelPlaces(CityType cityType);
    List<PlaceSimpleQueryDto> findRecommendTravelPlaces(ThemeType themeType);
}
