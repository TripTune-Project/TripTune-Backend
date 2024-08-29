package com.triptune.domain.travel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TravelLocationRequest {
    private double longitude;   // 경도
    private double latitude;    // 위도
}
