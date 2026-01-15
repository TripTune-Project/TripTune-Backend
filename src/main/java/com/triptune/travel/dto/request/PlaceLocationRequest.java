package com.triptune.travel.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class PlaceLocationRequest {

    @NotNull(message = "위도는 필수 입력 값입니다.")
    private Double latitude;    // 위도

    @NotNull(message = "경도는 필수 입력 값입니다.")
    private Double longitude;   // 경도


    @Builder
    public PlaceLocationRequest(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
