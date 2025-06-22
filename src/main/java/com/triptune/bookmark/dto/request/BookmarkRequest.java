package com.triptune.bookmark.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkRequest {
    // TODO: validation 확인
    @NotNull(message = "여행지 ID는 필수 입력 값입니다.")
    @Min(value = 1, message = "여행지 ID는 1 이상의 값이어야 합니다.")
    private Long placeId;

    @Builder
    public BookmarkRequest(Long placeId) {
        this.placeId = placeId;
    }
}
