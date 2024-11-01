package com.triptune.domain.travel.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceLocationRequest {

    @NotNull(message = "경도는 필수 입력 값입니다.")
    private double longitude;   // 경도

    @NotNull(message = "위도는 필수 입력 값입니다.")
    private double latitude;    // 위도

    @Builder
    public PlaceLocationRequest(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
