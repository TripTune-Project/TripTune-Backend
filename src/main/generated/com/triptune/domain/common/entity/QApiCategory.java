package com.triptune.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApiCategory is a Querydsl query type for ApiCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApiCategory extends EntityPathBase<ApiCategory> {

    private static final long serialVersionUID = 649855434L;

    public static final QApiCategory apiCategory = new QApiCategory("apiCategory");

    public final StringPath categoryCode = createString("categoryCode");

    public final StringPath categoryName = createString("categoryName");

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath parentCode = createString("parentCode");

    public final ListPath<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace> travelPlaceList = this.<com.triptune.domain.travel.entity.TravelPlace, com.triptune.domain.travel.entity.QTravelPlace>createList("travelPlaceList", com.triptune.domain.travel.entity.TravelPlace.class, com.triptune.domain.travel.entity.QTravelPlace.class, PathInits.DIRECT2);

    public QApiCategory(String variable) {
        super(ApiCategory.class, forVariable(variable));
    }

    public QApiCategory(Path<? extends ApiCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApiCategory(PathMetadata metadata) {
        super(ApiCategory.class, metadata);
    }

}

