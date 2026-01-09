package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMentorProfile is a Querydsl query type for MentorProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMentorProfile extends EntityPathBase<MentorProfile> {

    private static final long serialVersionUID = -810038900L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMentorProfile mentorProfile = new QMentorProfile("mentorProfile");

    public final NumberPath<Double> averageRating = createNumber("averageRating", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> deptId = createNumber("deptId", Long.class);

    public final NumberPath<Long> exp = createNumber("exp", Long.class);

    public final StringPath introduction = createString("introduction");

    public final BooleanPath isVerified = createBoolean("isVerified");

    public final NumberPath<Long> point = createNumber("point", Long.class);

    public final StringPath studentCardImg = createString("studentCardImg");

    public final NumberPath<Long> univId = createNumber("univId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUsers user;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QMentorProfile(String variable) {
        this(MentorProfile.class, forVariable(variable), INITS);
    }

    public QMentorProfile(Path<? extends MentorProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMentorProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMentorProfile(PathMetadata metadata, PathInits inits) {
        this(MentorProfile.class, metadata, inits);
    }

    public QMentorProfile(Class<? extends MentorProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

