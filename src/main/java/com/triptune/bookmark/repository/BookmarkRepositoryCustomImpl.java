package com.triptune.bookmark.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.bookmark.entity.QBookmark;
import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.travel.entity.QTravelPlace;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.global.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QBookmark bookmark;
    private final QTravelPlace travelPlace;


    public BookmarkRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
        this.bookmark = QBookmark.bookmark;
        this.travelPlace = QTravelPlace.travelPlace;
    }

    @Override
    public Page<TravelPlace> findSortedMemberBookmarks(Long memberId, Pageable pageable, BookmarkSortType sortType) {
        BooleanExpression expression = bookmark.member.memberId.eq(memberId);
        OrderSpecifier<?>[] sortTypes = getOrderBySortType(sortType);

        List<TravelPlace> content = jpaQueryFactory
                .select(travelPlace)
                .from(bookmark)
                .join(bookmark.travelPlace, travelPlace)
                .where(expression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortTypes)
                .fetch();

        int totalElements = countTotalElements(expression);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countTotalElements(BooleanExpression expression) {
        Long totalElements = jpaQueryFactory
                .select(bookmark.count())
                .from(bookmark)
                .where(expression)
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    public OrderSpecifier<?>[] getOrderBySortType(BookmarkSortType sortType){
        return switch (sortType) {
            case NEWEST -> new OrderSpecifier[] {
                    bookmark.createdAt.desc(),
                    travelPlace.placeId.desc()
            };
            case OLDEST -> new OrderSpecifier[] {
                    bookmark.createdAt.asc(),
                    travelPlace.placeId.asc()
            };
            case NAME ->  new OrderSpecifier[] {
                    travelPlace.placeName.asc(),
                    travelPlace.placeId.desc()
            };
        };
    }


}
