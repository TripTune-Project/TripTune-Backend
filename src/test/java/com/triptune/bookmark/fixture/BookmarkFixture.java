package com.triptune.bookmark.fixture;

import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.bookmark.repository.dto.PlaceBookmarkQueryDto;
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

    public static PlaceBookmarkQueryDto createPlaceBookmarkQueryDto(TravelPlace place, String thumbnailS3ObjectKey) {
        return PlaceBookmarkQueryDto.builder()
                .placeId(place.getPlaceId())
                .country(place.getCountry().getCountryName())
                .city(place.getCity().getCityName())
                .district(place.getDistrict().getDistrictName())
                .address(place.getAddress())
                .detailAddress(place.getDetailAddress())
                .placeName(place.getPlaceName())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .build();
    }
}
