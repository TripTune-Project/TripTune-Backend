package com.triptune.bookmark.fixture;

import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.member.entity.Member;
import com.triptune.travel.entity.TravelPlace;

public class BookmarkFixture {

    public static Bookmark createBookmark(Member member, TravelPlace travelPlace){
        return Bookmark.createBookmark(member, travelPlace);
    }

    public static BookmarkRequest createBookmarkRequest(Long placeId){
        return BookmarkRequest.builder()
                .placeId(placeId)
                .build();
    }
}
