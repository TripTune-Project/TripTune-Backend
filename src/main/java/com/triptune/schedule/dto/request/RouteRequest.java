package com.triptune.schedule.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class RouteRequest {
    private int routeOrder;
    private Long placeId;

    @Builder
    public RouteRequest(int routeOrder, Long placeId) {
        this.routeOrder = routeOrder;
        this.placeId = placeId;
    }

    public static RouteRequest of(int routeOrder, Long placeId){
        return new RouteRequest(routeOrder, placeId);
    }
}
