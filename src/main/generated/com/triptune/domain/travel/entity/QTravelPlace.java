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

    public final com.triptune.domain.common.entity.QApiCategory apiCategory;

    public final com.triptune.domain.common.entity.QApiContentType apiContentType;

    public final NumberPath<Integer> bookmarkCnt = createNumber("bookmarkCnt", Integer.class);

    public final StringPath checkInTime = createString("checkInTime");

    public final StringPath checkOutTime = createString("checkOutTime");

    public final com.triptune.domain.common.entity.QCity city;

    public final com.triptune.domain.common.entity.QCountry country;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final StringPath detailAddress = createString("detailAddress");

    public final com.triptune.domain.common.entity.QDistrict district;

    public final StringPath homepage = createString("homepage");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final NumberPath<Long> placeId = createNumber("placeId", Long.class);

    public final StringPath placeName = createString("placeName");

    public final ListPath<TravelImage, QTravelImage> travelImageList = this.<TravelImage, QTravelImage>createList("travelImageList", TravelImage.class, QTravelImage.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath useTime = createString("useTime");

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
        this.apiCategory = inits.isInitialized("apiCategory") ? new com.triptune.domain.common.entity.QApiCategory(forProperty("apiCategory")) : null;
        this.apiContentType = inits.isInitialized("apiContentType") ? new com.triptune.domain.common.entity.QApiContentType(forProperty("apiContentType")) : null;
        this.city = inits.isInitialized("city") ? new com.triptune.domain.common.entity.QCity(forProperty("city"), inits.get("city")) : null;
        this.country = inits.isInitialized("country") ? new com.triptune.domain.common.entity.QCountry(forProperty("country")) : null;
        this.district = inits.isInitialized("district") ? new com.triptune.domain.common.entity.QDistrict(forProperty("district"), inits.get("district")) : null;
    }

}

