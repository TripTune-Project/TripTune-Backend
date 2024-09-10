package com.triptune.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCountry is a Querydsl query type for Country
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCountry extends EntityPathBase<Country> {

    private static final long serialVersionUID = -900296408L;

    public static final QCountry country = new QCountry("country");

    public final ListPath<City, QCity> cityList = this.<City, QCity>createList("cityList", City.class, QCity.class, PathInits.DIRECT2);

    public final NumberPath<Long> CountryId = createNumber("CountryId", Long.class);

    public final StringPath countryName = createString("countryName");

    public final ListPath<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace> travelPlaceList = this.<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace>createList("travelPlaceList", com.triptune.domain.travel.entity.TravelPlace.class, com.triptune.domain.travel.entity.QTravelPlace.class, PathInits.DIRECT2);

    public QCountry(String variable) {
        super(Country.class, forVariable(variable));
    }

    public QCountry(Path<? extends Country> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCountry(PathMetadata metadata) {
        super(Country.class, metadata);
    }

}

