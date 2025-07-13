package com.triptune.schedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class RouteRequest {

    @NotNull(message = "여행 루트 순서는 필수 입력 값입니다.")
    @Min(value = 1, message = "여행 루트 순서는 1 이상의 값이어야 합니다.")
    private Integer routeOrder;

    @NotNull(message = "여행지 ID는 필수 입력 값입니다.")
    @Min(value = 1, message = "여행지 ID는 1 이상의 값이어야 합니다.")
    private Long placeId;

    @Builder
    public RouteRequest(Integer routeOrder, Long placeId) {
        this.routeOrder = routeOrder;
        this.placeId = placeId;
    }

    public static RouteRequest of(Integer routeOrder, Long placeId){
        return new RouteRequest(routeOrder, placeId);
    }
}
