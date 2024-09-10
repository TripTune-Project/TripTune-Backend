package com.triptune.domain.travel.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TravelSearchRequest {
    
    @NotEmpty(message = "경도는 필수 입력 값입니다.")
    private double longitude;   // 경도

    @NotEmpty(message = "위도는 필수 입력 값입니다.")
    private double latitude;    // 위도

    @NotEmpty(message = "검색어는 필수 입력 값입니다.")
    private String keyword;

    @Builder
    public TravelSearchRequest(double longitude, double latitude, String keyword) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.keyword = keyword;
    }
}
