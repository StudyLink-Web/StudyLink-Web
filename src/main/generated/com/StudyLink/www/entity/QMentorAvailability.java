package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMentorAvailability is a Querydsl query type for MentorAvailability
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMentorAvailability extends EntityPathBase<MentorAvailability> {

    private static final long serialVersionUID = -1959638184L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMentorAvailability mentorAvailability = new QMentorAvailability("mentorAvailability");

    public final NumberPath<Long> availId = createNumber("availId", Long.class);

    public final NumberPath<Integer> block = createNumber("block", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final QUsers mentor;

    public QMentorAvailability(String variable) {
        this(MentorAvailability.class, forVariable(variable), INITS);
    }

    public QMentorAvailability(Path<? extends MentorAvailability> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMentorAvailability(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMentorAvailability(PathMetadata metadata, PathInits inits) {
        this(MentorAvailability.class, metadata, inits);
    }

    public QMentorAvailability(Class<? extends MentorAvailability> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mentor = inits.isInitialized("mentor") ? new QUsers(forProperty("mentor"), inits.get("mentor")) : null;
    }

}

