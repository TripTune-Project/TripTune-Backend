package com.triptune.bookmark.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkRepositoryCustom {
    Page<TravelPlace> findSortedMemberBookmarks(Long memberId, Pageable pageable, BookmarkSortType sortType);
    Integer countTotalElements(BooleanExpression expression);
}
