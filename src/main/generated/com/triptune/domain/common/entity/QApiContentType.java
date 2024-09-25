package com.triptune.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApiContentType is a Querydsl query type for ApiContentType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApiContentType extends EntityPathBase<ApiContentType> {

    private static final long serialVersionUID = 498813703L;

    public static final QApiContentType apiContentType = new QApiContentType("apiContentType");

    public final NumberPath<Long> contentTypeId = createNumber("contentTypeId", Long.class);

    public final StringPath contentTypeName = createString("contentTypeName");

    public final ListPath<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace> travelPlaceList = this.<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace>createList("travelPlaceList", com.triptune.domain.travel.entity.TravelPlace.class, com.triptune.domain.travel.entity.QTravelPlace.class, PathInits.DIRECT2);

    public QApiContentType(String variable) {
        super(ApiContentType.class, forVariable(variable));
    }

    public QApiContentType(Path<? extends ApiContentType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApiContentType(PathMetadata metadata) {
        super(ApiContentType.class, metadata);
    }

}

