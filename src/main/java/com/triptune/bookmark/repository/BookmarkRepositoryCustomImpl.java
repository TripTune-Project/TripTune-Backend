package com.triptune.bookmark.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.bookmark.entity.QBookmark;
import com.triptune.bookmark.enumclass.BookmarkSortType;
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
    public Page<TravelPlace> findBookmarksByUserId(String userId, Pageable pageable, BookmarkSortType sortType) {
        BooleanExpression expression = bookmark.member.userId.eq(userId);
        OrderSpecifier<?> orderBySortType = getOrderBySortType(sortType);

        List<TravelPlace> content = jpaQueryFactory
                .select(travelPlace)
                .from(bookmark)
                .join(bookmark.travelPlace, travelPlace)
                .where(expression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderBySortType)
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

    public OrderSpecifier<?> getOrderBySortType(BookmarkSortType order){
        return switch (order) {
            case NEWEST -> bookmark.createdAt.desc();
            case OLDEST -> bookmark.createdAt.asc();
            case NAME -> travelPlace.placeName.asc();
        };
    }


}
