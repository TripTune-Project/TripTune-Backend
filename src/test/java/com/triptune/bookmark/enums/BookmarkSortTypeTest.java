package com.triptune.bookmark.enums;

import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.CustomIllegalArgumentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookmarkSortTypeTest {

    @Test
    @DisplayName("소문자로 들어온 값과 enum 매칭")
    void determineSortType(){
        // given, when
        BookmarkSortType response = BookmarkSortType.determineSortType("newest");

        // then
        assertThat(response).isEqualTo(BookmarkSortType.NEWEST);
    }

    @Test
    @DisplayName("소문자로 들어온 값과 enum 매칭 실패로 예외 발생")
    void determineSortType_Illegal(){
        // given, when, then
        CustomIllegalArgumentException fail = assertThrows(CustomIllegalArgumentException.class,
                () -> BookmarkSortType.determineSortType("fail"));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE);

    }

}