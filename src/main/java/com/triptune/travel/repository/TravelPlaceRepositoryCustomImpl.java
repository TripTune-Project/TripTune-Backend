package com.triptune.travel.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.repository.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.QTravelImage;
import com.triptune.travel.entity.QTravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.MathExpressions.*;
import static com.triptune.travel.entity.QTravelImage.travelImage;
import static com.triptune.travel.entity.QTravelPlace.travelPlace;

@Repository
@RequiredArgsConstructor
public class TravelPlaceRepositoryCustomImpl implements TravelPlaceRepositoryCustom {
    private static final int CAROUSEL_LIMIT = 20;

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Page<PlaceLocation> findNearByTravelPlaces(Pageable pageable, PlaceLocationRequest placeLocationRequest, int radius) {
        NumberExpression<Double> haversineExpression = getHaversineFormula(placeLocationRequest.getLatitude(), placeLocationRequest.getLongitude());

        BooleanExpression loeExpression = haversineExpression.loe(radius);

        List<PlaceLocation> content = jpaQueryFactory
                .select(Projections.constructor(PlaceLocation.class,
                        travelPlace.placeId,
                        travelPlace.country.countryName,
                        travelPlace.city.cityName,
                        travelPlace.district.districtName,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.latitude,
                        travelPlace.longitude,
                        travelPlace.placeName,
                        JPAExpressions
                                .select(travelImage.s3ObjectUrl)
                                .from(travelImage)
                                .where(travelImage.travelPlace.placeId.eq(travelPlace.placeId)
                                        .and(travelImage.isThumbnail.isTrue()))
                                .limit(1),
                        haversineExpression.as("distance")))
                .from(travelPlace)
                .where(loeExpression)
                .orderBy(
                        haversineExpression.asc(),
                        travelPlace.placeId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTotalElements(loeExpression);

        return PageUtils.createPage(content, pageable, totalElements);
    }



    @Override
    public Page<PlaceLocation> searchTravelPlacesWithLocation(Pageable pageable, PlaceSearchRequest placeSearchRequest) {
        String keyword = placeSearchRequest.getKeyword();

        BooleanExpression booleanExpression = travelPlace.country.countryName.contains(keyword)
                .or(travelPlace.city.cityName.contains(keyword))
                .or(travelPlace.district.districtName.contains(keyword))
                .or(travelPlace.placeName.contains(keyword));

        NumberExpression<Double> haversineExpression = getHaversineFormula(placeSearchRequest.getLatitude(), placeSearchRequest.getLongitude());
        String orderCaseString = accuracyQuery();

        List<PlaceLocation> content = jpaQueryFactory
                .select(Projections.constructor(PlaceLocation.class,
                        travelPlace.placeId,
                        travelPlace.country.countryName,
                        travelPlace.city.cityName,
                        travelPlace.district.districtName,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.latitude,
                        travelPlace.longitude,
                        travelPlace.placeName,
                        JPAExpressions
                                .select(travelImage.s3ObjectUrl)
                                .from(travelImage)
                                .where(travelImage.travelPlace.placeId.eq(travelPlace.placeId)
                                        .and(travelImage.isThumbnail.isTrue()))
                                .limit(1),
                        haversineExpression.as("distance")))
                .from(travelPlace)
                .where(booleanExpression)
                .orderBy(
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.placeName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.country.countryName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.city.cityName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.district.districtName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        travelPlace.placeId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTotalElements(booleanExpression);

        return PageUtils.createPage(content, pageable, totalElements);
    }


    private NumberExpression<Double> getHaversineFormula(double latDeg, double lonDeg){
        double earthRadius = 6371.0;

        // haversine 공식을 적용하여 거리 계산
        return acos(
                sin(radians(constant(latDeg)))
                        .multiply(sin(radians(travelPlace.latitude)))
                        .add(
                                cos(radians(constant(latDeg)))
                                        .multiply(cos(radians(travelPlace.latitude)))
                                        .multiply(cos(radians(constant(lonDeg))
                                                .subtract(radians(travelPlace.longitude))))
                        )
        ).multiply(earthRadius);
    }


    @Override
    public Page<PlaceResponse> searchTravelPlaces(Pageable pageable, String keyword) {
        BooleanExpression booleanExpression = travelPlace.country.countryName.contains(keyword)
                .or(travelPlace.city.cityName.contains(keyword))
                .or(travelPlace.district.districtName.contains(keyword))
                .or(travelPlace.placeName.contains(keyword));

        String orderCaseString = accuracyQuery();

        List<PlaceResponse> content = jpaQueryFactory
                .select(Projections.constructor(PlaceResponse.class,
                        travelPlace.placeId,
                        travelPlace.country.countryName,
                        travelPlace.city.cityName,
                        travelPlace.district.districtName,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.latitude,
                        travelPlace.longitude,
                        travelPlace.placeName,
                        JPAExpressions
                                .select(travelImage.s3ObjectUrl)
                                .from(travelImage)
                                .where(travelImage.travelPlace.placeId.eq(travelPlace.placeId)
                                        .and(travelImage.isThumbnail.isTrue()))
                                .limit(1)))
                .from(travelPlace)
                .where(booleanExpression)
                .orderBy(
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.placeName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.country.countryName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.city.cityName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelPlace.district.districtName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        travelPlace.placeId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTotalElements(booleanExpression);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Page<PlaceResponse> findDefaultTravelPlacesByJungGu(Pageable pageable) {
        double jungGuLatDeg = 37.56397;
        double jungGuLongDeg = 126.997688;

        NumberExpression<Double> haversineExpression = getHaversineFormula(jungGuLatDeg, jungGuLongDeg);

//        List<PlaceLocation> content = jpaQueryFactory
//                .select(Projections.constructor(PlaceLocation.class,
//                        travelPlace.placeId,
//                        travelPlace.country.countryName,
//                        travelPlace.city.cityName,
//                        travelPlace.district.districtName,
//                        travelPlace.address,
//                        travelPlace.detailAddress,
//                        travelPlace.latitude,
//                        travelPlace.longitude,
//                        travelPlace.placeName,
//                        JPAExpressions
//                                .select(travelImage.s3ObjectUrl)
//                                .from(travelImage)
//                                .where(travelImage.travelPlace.placeId.eq(travelPlace.placeId)
//                                        .and(travelImage.isThumbnail.isTrue()))
//                                .limit(1),
//                        haversineExpression.as("distance")))
//                .from(travelPlace)
//                .where(loeExpression)
//                .orderBy(
//                        haversineExpression.asc(),
//                        travelPlace.placeId.desc()
//                )
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//

        List<PlaceResponse> content = jpaQueryFactory
                .select(Projections.constructor(PlaceResponse.class,
                        travelPlace.placeId,
                        travelPlace.country.countryName,
                        travelPlace.city.cityName,
                        travelPlace.district.districtName,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.latitude,
                        travelPlace.longitude,
                        travelPlace.placeName,
                        JPAExpressions
                                .select(travelImage.s3ObjectUrl)
                                .from(travelImage)
                                .where(travelImage.travelPlace.placeId.eq(travelPlace.placeId)
                                        .and(travelImage.isThumbnail.isTrue()))
                                .limit(1)))
                .from(travelPlace)
                .orderBy(
                        haversineExpression.asc(),
                        travelPlace.placeId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTotalTravelPlaces();

        return PageUtils.createPage(content, pageable, totalElements);
    }

    private int countTotalTravelPlaces() {
        Long totalElements = jpaQueryFactory
                .select(travelPlace.count())
                .from(travelPlace)
                .fetchOne();

        if (totalElements == null) totalElements = 0L;
        return totalElements.intValue();
    }


    @Override
    public Integer countTotalElements(BooleanExpression expression) {
        Long totalElements = jpaQueryFactory
                .select(travelPlace.count())
                .from(travelPlace)
                .where(expression)
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public List<PlaceSimpleResponse> findPopularTravelPlaces(CityType cityType) {
        return jpaQueryFactory
                .select(Projections.constructor(PlaceSimpleResponse.class,
                        travelPlace.placeId,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.placeName,
                        travelImage.s3ObjectUrl
                ))
                .from(travelPlace)
                .leftJoin(travelPlace.travelImages, travelImage)
                .on(travelImage.isThumbnail.isTrue())
                .where(travelPlace.city.cityName.in(cityType.getDbCityGrouping()))
                .orderBy(
                        travelPlace.bookmarkCnt.desc(),
                        travelPlace.placeId.desc()
                )
                .limit(CAROUSEL_LIMIT)
                .fetch();
    }

    @Override
    public List<PlaceSimpleResponse> findRecommendTravelPlaces(ThemeType themeType) {
        return jpaQueryFactory
                .select(Projections.constructor(PlaceSimpleResponse.class,
                        travelPlace.placeId,
                        travelPlace.address,
                        travelPlace.detailAddress,
                        travelPlace.placeName,
                        travelImage.s3ObjectUrl))
                .from(travelPlace)
                .leftJoin(travelPlace.travelImages, travelImage)
                .on(travelImage.isThumbnail.isTrue())
                .where(themeTypeCondition(themeType))
                .orderBy(
                        travelPlace.bookmarkCnt.desc(),
                        travelPlace.placeId.desc()
                )
                .limit(CAROUSEL_LIMIT)
                .fetch();

    }



    private BooleanExpression themeTypeCondition(ThemeType themeType){
        if (themeType == ThemeType.All){
            return null;
        }

        return travelPlace.apiContentType.apiContentTypeId.eq(themeType.getApiContentTypeId());
    }

    private String accuracyQuery(){
        return "CASE " +
                "WHEN {0} = {1} THEN 0 " +
                "WHEN {0} LIKE {2} THEN 1 " +
                "WHEN {0} LIKE {3} THEN 2 " +
                "WHEN {0} LIKE {3} THEN 3 " +
                "ELSE 4 " +
                "END";
    }


}
