package com.triptune.route.fixture;

import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteRequest;
import com.triptune.route.dto.response.RouteCreateResponse;
import com.triptune.route.dto.response.RouteResponse;
import com.triptune.route.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.route.repository.dto.RouteQueryDto;
import com.triptune.travel.entity.TravelPlace;

public class TravelRouteFixture {

    public static TravelRoute createRoute(TravelSchedule schedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.createTravelRoute(
                schedule,
                travelPlace,
                routeOrder
        );
    }

    public static RouteRequest createRouteRequest(Integer routeOrder, Long placeId){
        return RouteRequest.of(routeOrder, placeId);
    }


    public static RouteCreateRequest createRouteCreateRequest(Long placeId, Integer routeVersion){
        return RouteCreateRequest.builder().placeId(placeId).routeVersion(routeVersion).build();
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

    public static RouteResponse createRouteResponse(int routeOrder, TravelPlace place, String thumbnailUrl) {
        return RouteResponse.builder()
                .routeOrder(routeOrder)
                .placeId(place.getPlaceId())
                .country(place.getCountry().getCountryName())
                .city(place.getCity().getCityName())
                .district(place.getDistrict().getDistrictName())
                .address(place.getAddress())
                .detailAddress(place.getDetailAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .placeName(place.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();

    }

    public static RouteCreateResponse createRouteCreateResponse(int routeVersion) {
        return RouteCreateResponse.builder()
                .routeVersion(routeVersion)
                .build();
    }
}
