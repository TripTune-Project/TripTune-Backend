package com.triptune.travel.enums;

import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.CustomIllegalArgumentException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum CityType {
    ALL("all", Set.of("서울", "부산", "제주도", "경기도", "강원특별자치도", "경상북도", "경상남도", "전북특별자치도", "전라남도", "충청북도", "충청남도")),
    SEOUL( "seoul", Set.of("서울")),
    BUSAN( "busan", Set.of("부산")),
    JEJU( "jeju", Set.of("제주도")),
    GYEONGGI("gyeonggi", Set.of("경기도")),
    GANGWON("gangwon", Set.of("강원특별자치도")),
    GYEONGSANG("gyeongsang", Set.of("경상북도", "경상남도")),
    JEOLLA("jeolla", Set.of("전북특별자치도", "전라남도")),
    CHUNGCHEONG("chungcheong", Set.of("충청북도", "충청남도"));

    private final String value;
    private final Set<String> dbCityGrouping;

    public static CityType from(String cityType){
        return Arrays.stream(CityType.values())
                .filter(city -> city.getValue().equals(cityType))
                .findFirst()
                .orElseThrow(() -> new CustomIllegalArgumentException(ErrorCode.ILLEGAL_CITY_TYPE));
    }
}
