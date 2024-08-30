package com.triptune.domain.travel.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTravelImageFile is a Querydsl query type for TravelImageFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTravelImageFile extends EntityPathBase<TravelImageFile> {

    private static final long serialVersionUID = -978208512L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTravelImageFile travelImageFile = new QTravelImageFile("travelImageFile");

    public final com.triptune.domain.common.entity.QFile file;

    public final QTravelPlace travelPlace;

    public QTravelImageFile(String variable) {
        this(TravelImageFile.class, forVariable(variable), INITS);
    }

    public QTravelImageFile(Path<? extends TravelImageFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTravelImageFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTravelImageFile(PathMetadata metadata, PathInits inits) {
        this(TravelImageFile.class, metadata, inits);
    }

    public QTravelImageFile(Class<? extends TravelImageFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.file = inits.isInitialized("file") ? new com.triptune.domain.common.entity.QFile(forProperty("file")) : null;
        this.travelPlace = inits.isInitialized("travelPlace") ? new QTravelPlace(forProperty("travelPlace"), inits.get("travelPlace")) : null;
    }

}

