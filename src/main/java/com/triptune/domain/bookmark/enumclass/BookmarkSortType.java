package com.triptune.domain.bookmark.enumclass;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomIllegalArgumentException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BookmarkSortType {

    NEWEST("newest", "최신 순"),
    OLDEST("oldest","오래된 순"),
    NAME("name","여행지 이름 순");

    private final String value;
    private final String description;

    public static BookmarkSortType from(String sort){
        return Arrays.stream(BookmarkSortType.values())
                .filter(type -> type.getValue().equals(sort))
                .findFirst()
                .orElseThrow(() -> new CustomIllegalArgumentException(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE));
    }

}
