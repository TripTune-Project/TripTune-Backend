package com.triptune.schedule.fixture;

import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
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
}
