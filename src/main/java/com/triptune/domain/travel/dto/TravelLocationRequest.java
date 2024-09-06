package com.triptune.domain.travel.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TravelLocationRequest {

    @NotEmpty(message = "경도는 필수 입력 값입니다.")
    private double longitude;   // 경도

    @NotEmpty(message = "경도는 필수 입력 값입니다.")
    private double latitude;    // 위도

    @Builder
    public TravelLocationRequest(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
