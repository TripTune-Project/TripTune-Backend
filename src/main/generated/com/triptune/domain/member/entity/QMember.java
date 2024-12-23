package com.triptune.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1407572279L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final ListPath<com.triptune.domain.bookmark.entity.Bookmark, com.triptune.domain.bookmark.entity.QBookmark> bookmarkList = this.<com.triptune.domain.bookmark.entity.Bookmark, com.triptune.domain.bookmark.entity.QBookmark>createList("bookmarkList", com.triptune.domain.bookmark.entity.Bookmark.class, com.triptune.domain.bookmark.entity.QBookmark.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath isSocialLogin = createBoolean("isSocialLogin");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final QProfileImage profileImage;

    public final StringPath refreshToken = createString("refreshToken");

    public final ListPath<com.triptune.domain.schedule.entity.TravelAttendee, com.triptune.domain.schedule.entity.QTravelAttendee> travelAttendeeList = this.<com.triptune.domain.schedule.entity.TravelAttendee, com.triptune.domain.schedule.entity.QTravelAttendee>createList("travelAttendeeList", com.triptune.domain.schedule.entity.TravelAttendee.class, com.triptune.domain.schedule.entity.QTravelAttendee.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.profileImage = inits.isInitialized("profileImage") ? new QProfileImage(forProperty("profileImage"), inits.get("profileImage")) : null;
    }

}

