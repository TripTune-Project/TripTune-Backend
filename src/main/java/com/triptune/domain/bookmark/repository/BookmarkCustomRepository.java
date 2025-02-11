package com.triptune.domain.bookmark.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.domain.bookmark.enumclass.BookmarkSortType;
import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkCustomRepository {
    Page<TravelPlace> getBookmarkTravelPlaces(String userId, Pageable pageable, BookmarkSortType sortType);
    Integer countTotalElements(BooleanExpression expression);
}
