package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudentProfile is a Querydsl query type for StudentProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudentProfile extends EntityPathBase<StudentProfile> {

    private static final long serialVersionUID = 309599370L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudentProfile studentProfile = new QStudentProfile("studentProfile");

    public final NumberPath<Integer> bonusPoint = createNumber("bonusPoint", Integer.class);

    public final NumberPath<Integer> chargedPoint = createNumber("chargedPoint", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath regionPreference = createString("regionPreference");

    public final StringPath targetMajor = createString("targetMajor");

    public final StringPath targetUniversity = createString("targetUniversity");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUsers user;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QStudentProfile(String variable) {
        this(StudentProfile.class, forVariable(variable), INITS);
    }

    public QStudentProfile(Path<? extends StudentProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudentProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudentProfile(PathMetadata metadata, PathInits inits) {
        this(StudentProfile.class, metadata, inits);
    }

    public QStudentProfile(Class<? extends StudentProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

