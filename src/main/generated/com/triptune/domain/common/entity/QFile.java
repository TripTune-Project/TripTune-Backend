package com.triptune.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFile is a Querydsl query type for File
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFile extends EntityPathBase<File> {

    private static final long serialVersionUID = -48532182L;

    public static final QFile file = new QFile("file");

    public final StringPath apiFileUrl = createString("apiFileUrl");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Double> fileSize = createNumber("fileSize", Double.class);

    public final StringPath fileType = createString("fileType");

    public final BooleanPath isThumbnail = createBoolean("isThumbnail");

    public final StringPath originalName = createString("originalName");

    public final StringPath s3ObjectUrl = createString("s3ObjectUrl");

    public final ListPath<com.triptune.domain.travel.entity.TravelImage, com.triptune.domain.travel.entity.QTravelImage> travelImageFileList = this.<com.triptune.domain.travel.entity.TravelImage, com.triptune.domain.travel.entity.QTravelImage>createList("travelImageFileList", com.triptune.domain.travel.entity.TravelImage.class, com.triptune.domain.travel.entity.QTravelImage.class, PathInits.DIRECT2);

    public QFile(String variable) {
        super(File.class, forVariable(variable));
    }

    public QFile(Path<? extends File> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFile(PathMetadata metadata) {
        super(File.class, metadata);
    }

}

