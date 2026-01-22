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

    public final StringPath availableTime = createString("availableTime");

    public final NumberPath<Double> averageRating = createNumber("averageRating", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath credentials = createString("credentials");

    public final NumberPath<Long> deptId = createNumber("deptId", Long.class);

    public final NumberPath<Integer> entranceYear = createNumber("entranceYear", Integer.class);

    public final NumberPath<Long> exp = createNumber("exp", Long.class);

    public final StringPath grades = createString("grades");

    public final NumberPath<Integer> graduationYear = createNumber("graduationYear", Integer.class);

    public final StringPath introduction = createString("introduction");

    public final BooleanPath isVerified = createBoolean("isVerified");

    public final NumberPath<Long> lessonCount = createNumber("lessonCount", Long.class);

    public final StringPath lessonLocation = createString("lessonLocation");

    public final StringPath lessonType = createString("lessonType");

    public final StringPath major = createString("major");

    public final StringPath mentorNickname = createString("mentorNickname");

    public final NumberPath<Double> minLessonHours = createNumber("minLessonHours", Double.class);

    public final BooleanPath notificationLesson = createBoolean("notificationLesson");

    public final BooleanPath notificationMessage = createBoolean("notificationMessage");

    public final BooleanPath notificationReview = createBoolean("notificationReview");

    public final NumberPath<Long> point = createNumber("point", Long.class);

    public final NumberPath<Integer> pricePerHour = createNumber("pricePerHour", Integer.class);

    public final NumberPath<Long> reviewCount = createNumber("reviewCount", Long.class);

    public final StringPath studentCardImg = createString("studentCardImg");

    public final StringPath subjects = createString("subjects");

    public final StringPath university = createString("university");

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

