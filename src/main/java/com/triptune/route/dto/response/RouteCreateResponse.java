package com.triptune.route.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteCreateResponse {
    private Long routeId;
    private Integer routeVersion;

    @Builder
    public RouteCreateResponse(Long routeId, Integer routeVersion) {
        this.routeId = routeId;
        this.routeVersion = routeVersion;
    }

    public static RouteCreateResponse of(Long routeId, Integer routeVersion){
        return RouteCreateResponse.builder()
                .routeId(routeId)
                .routeVersion(routeVersion)
                .build();
    }
}
