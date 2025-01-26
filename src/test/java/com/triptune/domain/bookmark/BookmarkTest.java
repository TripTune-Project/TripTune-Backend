package com.triptune.domain.bookmark;

import com.triptune.domain.BaseTest;
import com.triptune.domain.bookmark.dto.request.BookmarkRequest;

public class BookmarkTest extends BaseTest {
    protected BookmarkRequest createBookmarkRequest(Long placeId){
        return BookmarkRequest.builder()
                .placeId(placeId)
                .build();
    }
}
