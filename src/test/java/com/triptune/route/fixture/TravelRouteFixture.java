package com.triptune.route.fixture;

import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteUpdateRequest;
import com.triptune.route.dto.response.RouteCreateResponse;
import com.triptune.route.dto.response.RouteResponse;
import com.triptune.route.dto.response.RouteVersionResponse;
import com.triptune.route.entity.TravelRoute;
import com.triptune.route.repository.dto.RouteQueryDto;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.travel.entity.TravelPlace;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class TravelRouteFixture {

    public static TravelRoute createRoute(TravelSchedule schedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.createTravelRoute(
                schedule,
                travelPlace,
                routeOrder
        );
    }


    public static TravelRoute createRouteWithId(Long routeId, TravelSchedule schedule, TravelPlace travelPlace, int routeOrder) {
        TravelRoute route = TravelRoute.createTravelRoute(
                schedule,
                travelPlace,
                routeOrder
        );

        ReflectionTestUtils.setField(route, "routeId", routeId);
        return route;
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

    public static RouteCreateResponse createRouteCreateResponse(Long routeId, int routeVersion) {
        return RouteCreateResponse.builder()
                .routeId(routeId)
                .routeVersion(routeVersion)
                .build();
    }

    public static RouteVersionResponse createRouteVersionResponse(int routeVersion) {
        return RouteVersionResponse.builder()
                .routeVersion(routeVersion)
                .build();
    }

    public static RouteUpdateRequest createRouteUpdateRequest(Integer routeVersion,List<Long> routeIds) {
        return RouteUpdateRequest.builder()
                .routeVersion(routeVersion)
                .orderedRouteIds(routeIds)
                .build();
    }

}
