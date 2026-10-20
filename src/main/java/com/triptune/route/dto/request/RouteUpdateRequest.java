package com.triptune.route.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RouteUpdateRequest {

    @NotNull(message = "여행 루트 버전은 필수 입력 값입니다.")
    @Min(value = 0, message = "여행 루트 버전은 0 이상이어야 합니다.")
    private Integer routeVersion;

    @NotNull(message = "여행 루트 ID는 필수 입력 값입니다.")
    private List<Long> orderedRouteIds;

    @Builder
    public RouteUpdateRequest(Integer routeVersion, List<Long> orderedRouteIds) {
        this.routeVersion = routeVersion;
        this.orderedRouteIds = orderedRouteIds;
    }
}
