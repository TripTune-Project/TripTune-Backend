package com.triptune.travel.enums;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.CustomIllegalArgumentException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ThemeType {
    All("all", null,  null),
    ATTRACTIONS("attractions", "관광지", 12),
    CULTURE("culture", "문화시설", 14),
    SPORTS("sports", "레포츠",28),
    LODGING("lodging", "숙박", 32),
    SHOPPING("shopping", "쇼핑", 38),
    FOOD("food", "음식점", 39);

    private final String value;
    private final String apiContentTypeName;
    private final Integer apiContentTypeId;

    public static ThemeType from(String themeType){
        return Arrays.stream(ThemeType.values())
                .filter(theme -> theme.getValue().equals(themeType))
                .findFirst()
                .orElseThrow(() -> new CustomIllegalArgumentException(ErrorCode.ILLEGAL_THEME_TYPE));
    }



}
