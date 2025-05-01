package com.triptune.bookmark.enums;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.CustomIllegalArgumentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookmarkSortTypeTest {

    @Test
    @DisplayName("소문자로 들어온 값과 enum 매칭")
    void from(){
        // given, when
        BookmarkSortType response = BookmarkSortType.from("newest");

        // then
        assertThat(response).isEqualTo(BookmarkSortType.NEWEST);
    }

    @Test
    @DisplayName("소문자로 들어온 값과 enum 매칭 실패로 예외 발생")
    void from_Illegal(){
        // given, when, then
        assertThatThrownBy(() -> BookmarkSortType.from("fail"))
                .isInstanceOf(CustomIllegalArgumentException.class)
                .hasMessage(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE.getMessage());

    }

}