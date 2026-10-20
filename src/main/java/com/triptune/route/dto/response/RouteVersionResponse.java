package com.triptune.route.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteVersionResponse {
    private Integer routeVersion;

    @Builder
    public RouteVersionResponse(Integer routeVersion) {
        this.routeVersion = routeVersion;
    }

    public static RouteVersionResponse of(Integer routeVersion){
        return RouteVersionResponse.builder()
                .routeVersion(routeVersion)
                .build();
    }
}
