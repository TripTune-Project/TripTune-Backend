package com.triptune.domain.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTravelAttendee is a Querydsl query type for TravelAttendee
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTravelAttendee extends EntityPathBase<TravelAttendee> {

    private static final long serialVersionUID = 320279118L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTravelAttendee travelAttendee = new QTravelAttendee("travelAttendee");

    public final NumberPath<Long> attendeeId = createNumber("attendeeId", Long.class);

    public final com.triptune.domain.member.entity.QMember member;

    public final EnumPath<com.triptune.domain.schedule.enumclass.AttendeePermission> permission = createEnum("permission", com.triptune.domain.schedule.enumclass.AttendeePermission.class);

    public final EnumPath<com.triptune.domain.schedule.enumclass.AttendeeRole> role = createEnum("role", com.triptune.domain.schedule.enumclass.AttendeeRole.class);

    public final QTravelSchedule travelSchedule;

    public QTravelAttendee(String variable) {
        this(TravelAttendee.class, forVariable(variable), INITS);
    }

    public QTravelAttendee(Path<? extends TravelAttendee> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTravelAttendee(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTravelAttendee(PathMetadata metadata, PathInits inits) {
        this(TravelAttendee.class, metadata, inits);
    }

    public QTravelAttendee(Class<? extends TravelAttendee> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.triptune.domain.member.entity.QMember(forProperty("member")) : null;
        this.travelSchedule = inits.isInitialized("travelSchedule") ? new QTravelSchedule(forProperty("travelSchedule")) : null;
    }

}

