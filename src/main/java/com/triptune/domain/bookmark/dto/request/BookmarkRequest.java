package com.triptune.domain.bookmark.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkRequest {

    @NotNull(message = "여행지 아이디는 필수 입력 값입니다.")
    private Long placeId;

    @Builder
    public BookmarkRequest(Long placeId) {
        this.placeId = placeId;
    }
}
