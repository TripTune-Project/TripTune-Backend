package com.triptune.domain.travel.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTravelImage is a Querydsl query type for TravelImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTravelImage extends EntityPathBase<TravelImage> {

    private static final long serialVersionUID = -1631084188L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTravelImage travelImage = new QTravelImage("travelImage");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Double> fileSize = createNumber("fileSize", Double.class);

    public final StringPath fileType = createString("fileType");

    public final BooleanPath isThumbnail = createBoolean("isThumbnail");

    public final StringPath originalName = createString("originalName");

    public final StringPath s3ObjectUrl = createString("s3ObjectUrl");

    public final NumberPath<Long> travelImageId = createNumber("travelImageId", Long.class);

    public final QTravelPlace travelPlace;

    public QTravelImage(String variable) {
        this(TravelImage.class, forVariable(variable), INITS);
    }

    public QTravelImage(Path<? extends TravelImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTravelImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTravelImage(PathMetadata metadata, PathInits inits) {
        this(TravelImage.class, metadata, inits);
    }

    public QTravelImage(Class<? extends TravelImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.travelPlace = inits.isInitialized("travelPlace") ? new QTravelPlace(forProperty("travelPlace"), inits.get("travelPlace")) : null;
    }

}

