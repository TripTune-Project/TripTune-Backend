package com.triptune.schedule.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.global.util.PageUtils;
import com.triptune.schedule.repository.dto.RouteQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.triptune.schedule.entity.QTravelRoute.travelRoute;
import static com.triptune.travel.entity.QTravelImage.travelImage;

@Repository
@RequiredArgsConstructor
public class TravelRouteRepositoryCustomImpl implements TravelRouteRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<RouteQueryDto> findAllByScheduleId(Pageable pageable, Long scheduleId) {
        BooleanExpression expression = travelRoute.travelSchedule.scheduleId.eq(scheduleId);

        List<RouteQueryDto> contents = jpaQueryFactory
                .select(Projections.constructor(RouteQueryDto.class,
                        travelRoute.routeOrder,
                        travelRoute.travelPlace.placeId,
                        travelRoute.travelPlace.country.countryName,
                        travelRoute.travelPlace.city.cityName,
                        travelRoute.travelPlace.district.districtName,
                        travelRoute.travelPlace.address,
                        travelRoute.travelPlace.detailAddress,
                        travelRoute.travelPlace.latitude,
                        travelRoute.travelPlace.longitude,
                        travelRoute.travelPlace.placeName,
                        findThumbnailS3ObjectKey()))
                .from(travelRoute)
                .where(expression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(travelRoute.routeOrder.asc())
                .fetch();

        int totalElements = countTotalElements(expression);
        return PageUtils.createPage(contents, pageable, totalElements);
    }

    public JPQLQuery<String> findThumbnailS3ObjectKey(){
        return JPAExpressions
                .select(travelImage.s3ObjectKey)
                .from(travelImage)
                .where(travelImage.travelPlace.placeId.eq(travelRoute.travelPlace.placeId),
                        travelImage.isThumbnail.isTrue())
                .limit(1);
    }

    @Override
    public Integer countTotalElements(BooleanExpression expression){
        Long totalElements = jpaQueryFactory
                .select(travelRoute.count())
                .from(travelRoute)
                .where(expression)
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }
}
