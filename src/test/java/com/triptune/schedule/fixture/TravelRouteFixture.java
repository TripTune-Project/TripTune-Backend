package com.triptune.schedule.fixture;

import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.repository.dto.RouteQueryDto;
import com.triptune.travel.entity.TravelPlace;

public class TravelRouteFixture {

    public static TravelRoute createTravelRoute(TravelSchedule schedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.createTravelRoute(
                schedule,
                travelPlace,
                routeOrder
        );
    }

    public static RouteRequest createRouteRequest(Integer routeOrder, Long placeId){
        return RouteRequest.of(routeOrder, placeId);
    }


    public static RouteCreateRequest createRouteCreateRequest(Long placeId){
        return RouteCreateRequest.builder().placeId(placeId).build();
    }

    public static RouteQueryDto createRouteQueryDto(TravelRoute route, String thumbnailS3ObjectKey) {
        TravelPlace place = route.getTravelPlace();

        return RouteQueryDto.builder()
                .routeOrder(route.getRouteOrder())
                .placeId(place.getPlaceId())
                .country(place.getCountry().getCountryName())
                .city(place.getCity().getCityName())
                .district(place.getDistrict().getDistrictName())
                .address(place.getAddress())
                .detailAddress(place.getDetailAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .placeName(place.getPlaceName())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .build();
    }
}
