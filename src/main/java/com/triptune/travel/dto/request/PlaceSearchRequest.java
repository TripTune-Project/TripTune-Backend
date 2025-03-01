package com.triptune.travel.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceSearchRequest {

    @Nullable
    private Double longitude;   // 경도

    @Nullable
    private Double latitude;    // 위도

    @NotEmpty(message = "검색어는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣0-9ㄱ-ㅎㅏ-ㅣ\\s]*$", message = "검색어에 특수문자는 사용 불가합니다.")
    private String keyword;

    @Builder
    public PlaceSearchRequest(double longitude, double latitude, String keyword) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.keyword = keyword;
    }
}
