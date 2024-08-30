package com.triptune.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDistrict is a Querydsl query type for District
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDistrict extends EntityPathBase<District> {

    private static final long serialVersionUID = -1478415300L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDistrict district = new QDistrict("district");

    public final NumberPath<Integer> apiSigunguCode = createNumber("apiSigunguCode", Integer.class);

    public final QCity city;

    public final NumberPath<Long> districtId = createNumber("districtId", Long.class);

    public final StringPath districtName = createString("districtName");

    public final ListPath<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace> travelPlaceList = this.<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace>createList("travelPlaceList", com.triptune.domain.travel.entity.TravelPlace.class, com.triptune.domain.travel.entity.QTravelPlace.class, PathInits.DIRECT2);

    public QDistrict(String variable) {
        this(District.class, forVariable(variable), INITS);
    }

    public QDistrict(Path<? extends District> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDistrict(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDistrict(PathMetadata metadata, PathInits inits) {
        this(District.class, metadata, inits);
    }

    public QDistrict(Class<? extends District> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.city = inits.isInitialized("city") ? new QCity(forProperty("city"), inits.get("city")) : null;
    }

}

