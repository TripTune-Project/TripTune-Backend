package com.triptune.bookmark;

import com.triptune.BaseTest;
import com.triptune.bookmark.dto.request.BookmarkRequest;

public class BookmarkTest extends BaseTest {
    protected BookmarkRequest createBookmarkRequest(Long placeId){
        return BookmarkRequest.builder()
                .placeId(placeId)
                .build();
    }
}
