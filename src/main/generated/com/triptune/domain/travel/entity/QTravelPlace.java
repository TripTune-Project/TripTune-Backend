package com.triptune.domain.travel.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTravelPlace is a Querydsl query type for TravelPlace
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTravelPlace extends EntityPathBase<TravelPlace> {

    private static final long serialVersionUID = -1624649456L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTravelPlace travelPlace = new QTravelPlace("travelPlace");

    public final StringPath address = createString("address");

    public final NumberPath<Integer> apiContentId = createNumber("apiContentId", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> apiCreatedAt = createDateTime("apiCreatedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> apiUpdatedAt = createDateTime("apiUpdatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> bookmarkCnt = createNumber("bookmarkCnt", Integer.class);

    public final com.triptune.domain.common.entity.QCategory category;

    public final com.triptune.domain.common.entity.QCity city;

    public final NumberPath<Long> contentTypeId = createNumber("contentTypeId", Long.class);

    public final com.triptune.domain.common.entity.QCountry country;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final StringPath detailAddress = createString("detailAddress");

    public final com.triptune.domain.common.entity.QDistrict district;

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final NumberPath<Long> placeId = createNumber("placeId", Long.class);

    public final StringPath placeName = createString("placeName");

    public final ListPath<TravelImageFile, QTravelImageFile> travelImageFileList = this.<TravelImageFile, QTravelImageFile>createList("travelImageFileList", TravelImageFile.class, QTravelImageFile.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QTravelPlace(String variable) {
        this(TravelPlace.class, forVariable(variable), INITS);
    }

    public QTravelPlace(Path<? extends TravelPlace> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTravelPlace(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTravelPlace(PathMetadata metadata, PathInits inits) {
        this(TravelPlace.class, metadata, inits);
    }

    public QTravelPlace(Class<? extends TravelPlace> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.triptune.domain.common.entity.QCategory(forProperty("category")) : null;
        this.city = inits.isInitialized("city") ? new com.triptune.domain.common.entity.QCity(forProperty("city"), inits.get("city")) : null;
        this.country = inits.isInitialized("country") ? new com.triptune.domain.common.entity.QCountry(forProperty("country")) : null;
        this.district = inits.isInitialized("district") ? new com.triptune.domain.common.entity.QDistrict(forProperty("district"), inits.get("district")) : null;
    }

}

