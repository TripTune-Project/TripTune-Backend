package com.triptune.route.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteCreateResponse {
    private Integer routeVersion;

    @Builder
    public RouteCreateResponse(Integer routeVersion) {
        this.routeVersion = routeVersion;
    }

    public static RouteCreateResponse of(Integer routeVersion){
        return RouteCreateResponse.builder()
                .routeVersion(routeVersion)
                .build();
    }
}
