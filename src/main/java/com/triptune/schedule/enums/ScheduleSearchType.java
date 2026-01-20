package com.triptune.schedule.enums;

import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.CustomIllegalArgumentException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ScheduleSearchType {

    ALL("all", "전체 일정 중 검색"),
    SHARE("share", "공유된 일정 중 검색");

    private final String value;
    private final String description;

    public static ScheduleSearchType from(String typeStr){
        return Arrays.stream(ScheduleSearchType.values())
                .filter(type -> type.value.equals(typeStr))
                .findFirst()
                .orElseThrow(() -> new CustomIllegalArgumentException(ErrorCode.ILLEGAL_SCHEDULE_SEARCH_TYPE));
    }

    public boolean isAll() {
        return this == ALL;
    }

}
