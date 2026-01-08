package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMentorAvailability is a Querydsl query type for MentorAvailability
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMentorAvailability extends EntityPathBase<MentorAvailability> {

    private static final long serialVersionUID = -1959638184L;

    public static final QMentorAvailability mentorAvailability = new QMentorAvailability("mentorAvailability");

    public final NumberPath<Long> availabilityId = createNumber("availabilityId", Long.class);

    public final NumberPath<Integer> block = createNumber("block", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final NumberPath<Long> mentorId = createNumber("mentorId", Long.class);

    public QMentorAvailability(String variable) {
        super(MentorAvailability.class, forVariable(variable));
    }

    public QMentorAvailability(Path<? extends MentorAvailability> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMentorAvailability(PathMetadata metadata) {
        super(MentorAvailability.class, metadata);
    }

}

