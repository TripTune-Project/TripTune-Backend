package com.triptune.domain.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTravelRoute is a Querydsl query type for TravelRoute
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTravelRoute extends EntityPathBase<TravelRoute> {

    private static final long serialVersionUID = 1872327989L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTravelRoute travelRoute = new QTravelRoute("travelRoute");

    public final NumberPath<Long> routeId = createNumber("routeId", Long.class);

    public final NumberPath<Integer> routeOrder = createNumber("routeOrder", Integer.class);

    public final com.triptune.domain.travel.entity.QTravelPlace travelPlace;

    public final QTravelSchedule travelSchedule;

    public QTravelRoute(String variable) {
        this(TravelRoute.class, forVariable(variable), INITS);
    }

    public QTravelRoute(Path<? extends TravelRoute> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTravelRoute(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTravelRoute(PathMetadata metadata, PathInits inits) {
        this(TravelRoute.class, metadata, inits);
    }

    public QTravelRoute(Class<? extends TravelRoute> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.travelPlace = inits.isInitialized("travelPlace") ? new com.triptune.domain.travel.entity.QTravelPlace(forProperty("travelPlace"), inits.get("travelPlace")) : null;
        this.travelSchedule = inits.isInitialized("travelSchedule") ? new QTravelSchedule(forProperty("travelSchedule")) : null;
    }

}

