package com.triptune.domain.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTravelSchedule is a Querydsl query type for TravelSchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTravelSchedule extends EntityPathBase<TravelSchedule> {

    private static final long serialVersionUID = -920397781L;

    public static final QTravelSchedule travelSchedule = new QTravelSchedule("travelSchedule");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> scheduleId = createNumber("scheduleId", Long.class);

    public final StringPath scheduleName = createString("scheduleName");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final ListPath<TravelAttendee, QTravelAttendee> travelAttendeeList = this.<TravelAttendee, QTravelAttendee>createList("travelAttendeeList", TravelAttendee.class, QTravelAttendee.class, PathInits.DIRECT2);

    public final ListPath<TravelRoute, QTravelRoute> travelRouteList = this.<TravelRoute, QTravelRoute>createList("travelRouteList", TravelRoute.class, QTravelRoute.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QTravelSchedule(String variable) {
        super(TravelSchedule.class, forVariable(variable));
    }

    public QTravelSchedule(Path<? extends TravelSchedule> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTravelSchedule(PathMetadata metadata) {
        super(TravelSchedule.class, metadata);
    }

}

