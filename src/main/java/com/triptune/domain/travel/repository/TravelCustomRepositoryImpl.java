package com.triptune.domain.travel.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.entity.QTravelPlace;
import com.triptune.domain.travel.entity.TravelPlace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.lang.Math.acos;
import static java.lang.Math.cos;

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
        Predicate predicate = travelPlace.country.countryName.eq(country)
                .and(travelPlace.city.cityName.eq(city))
                .and(travelPlace.district.districtName.eq(district));


        List<TravelPlace> content = jpaQueryFactory
                .selectFrom(travelPlace)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 갯수 조회
        int totalElements = getTotalElements(predicate);

        return new PageImpl<>(content, pageable, totalElements);
    }


    @Override
    public Integer getTotalElements(Predicate predicate) {
        Long totalElements = jpaQueryFactory
                .select(travelPlace.count())
                .from(travelPlace)
                .where(predicate)
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }


}
