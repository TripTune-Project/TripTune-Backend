package com.triptune.bookmark.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.bookmark.repository.dto.PlaceBookmarkQueryDto;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.triptune.bookmark.entity.QBookmark.bookmark;
import static com.triptune.travel.entity.QTravelImage.travelImage;
import static com.triptune.travel.entity.QTravelPlace.travelPlace;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public Page<PlaceBookmarkQueryDto> findSortedMemberBookmarks(Long memberId, Pageable pageable, BookmarkSortType sortType) {
        BooleanExpression expression = bookmark.member.memberId.eq(memberId);
        OrderSpecifier<?>[] sortTypes = getOrderBySortType(sortType);

        List<PlaceBookmarkQueryDto> content = jpaQueryFactory
                .select(Projections.constructor(PlaceBookmarkQueryDto.class,
                        travelPlace.placeId,
                        travelPlace.country.countryName,
                        travelPlace.city.cityName,
                        travelPlace.district.districtName,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.placeName,
                        findThumbnailS3ObjectKey()))
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

    private JPQLQuery<String> findThumbnailS3ObjectKey(){
        return JPAExpressions
                .select(travelImage.s3ObjectKey)
                .from(travelImage)
                .where(travelImage.travelPlace.placeId.eq(travelPlace.placeId),
                        travelImage.isThumbnail.isTrue())
                .limit(1);

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
