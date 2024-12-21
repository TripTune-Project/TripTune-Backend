package com.triptune.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCity is a Querydsl query type for City
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCity extends EntityPathBase<City> {

    private static final long serialVersionUID = -48621287L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCity city = new QCity("city");

    public final NumberPath<Integer> apiAreaCode = createNumber("apiAreaCode", Integer.class);

    public final NumberPath<Long> cityId = createNumber("cityId", Long.class);

    public final StringPath cityName = createString("cityName");

    public final QCountry country;

    public final ListPath<District, QDistrict> districtList = this.<District, QDistrict>createList("districtList", District.class, QDistrict.class, PathInits.DIRECT2);

    public final ListPath<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace> travelPlaceList = this.<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace>createList("travelPlaceList", com.triptune.domain.travel.entity.TravelPlace.class, com.triptune.domain.travel.entity.QTravelPlace.class, PathInits.DIRECT2);

    public QCity(String variable) {
        this(City.class, forVariable(variable), INITS);
    }

    public QCity(Path<? extends City> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCity(PathMetadata metadata, PathInits inits) {
        this(City.class, metadata, inits);
    }

    public QCity(Class<? extends City> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountry(forProperty("country")) : null;
    }

}

