package com.triptune.domain.travel.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocationResponse;
import com.triptune.domain.travel.entity.QTravelPlace;
import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.entity.TravelPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.MathExpressions.*;

@Repository
public class TravelCustomRepositoryImpl implements TravelCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;
    private final QTravelPlace travelPlace;

    public TravelCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory){
        this.jpaQueryFactory = jpaQueryFactory;
        this.travelPlace = QTravelPlace.travelPlace;
    }

    @Override
    public Page<TravelPlace> findAllByAreaData(Pageable pageable, String country, String city, String district) {
        BooleanExpression expression = travelPlace.country.countryName.eq(country)
                .and(travelPlace.city.cityName.eq(city))
                .and(travelPlace.district.districtName.eq(district));


        List<TravelPlace> content = jpaQueryFactory
                .selectFrom(travelPlace)
                .where(expression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 갯수 조회
        int totalElements = getTotalElements(expression);

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Page<TravelLocationResponse> findNearByTravelPlaceList(Pageable pageable, TravelLocationRequest travelLocationRequest, int radius) {
        double earthRadius = 6371.0;

        double latRad = travelLocationRequest.getLatitude();
        double lonRad = travelLocationRequest.getLongitude();

        // harversine 공식을 적용하여 거리 계산
        NumberExpression<Double> harversineExpression = acos(
                sin(radians(constant(latRad)))
                        .multiply(sin(radians(travelPlace.latitude)))
                        .add(
                                cos(radians(constant(latRad)))
                                        .multiply(cos(radians(travelPlace.latitude)))
                                        .multiply(cos(radians(constant(lonRad)).subtract(radians(travelPlace.longitude))))
                        )
        ).multiply(earthRadius);

        BooleanExpression loeExpression = harversineExpression.loe(radius);

        List<TravelLocationResponse> content = jpaQueryFactory
                .select(Projections.constructor(TravelLocationResponse.class,
                        travelPlace.placeId,
                        travelPlace.country.countryName,
                        travelPlace.city.cityName,
                        travelPlace.district.districtName,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.longitude,
                        travelPlace.latitude,
                        travelPlace.placeName,
                        harversineExpression.as("distance")))
                .from(travelPlace)
                .where(loeExpression)
                .orderBy(harversineExpression.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = getTotalElements(loeExpression);

        return new PageImpl<>(content, pageable, totalElements);
    }


    @Override
    public Page<TravelPlace> searchTravelPlace(Pageable pageable, String type, String keyword) {
        BooleanExpression booleanExpression = null;

        switch(type){
            case "국가명":
                booleanExpression = travelPlace.country.countryName.contains(keyword);
                break;
            case "도시명":
                booleanExpression = travelPlace.city.cityName.contains(keyword);
                break;
            case "장소명":
                booleanExpression = travelPlace.placeName.contains(keyword);
                break;
        }

        List<TravelPlace> content = jpaQueryFactory
                .selectFrom(travelPlace)
                .where(booleanExpression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = getTotalElements(booleanExpression);

        return new PageImpl<>(content, pageable, totalElements);
    }


    @Override
    public Integer getTotalElements(BooleanExpression expression) {
        Long totalElements = jpaQueryFactory
                .select(travelPlace.count())
                .from(travelPlace)
                .where(expression)
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }




}
