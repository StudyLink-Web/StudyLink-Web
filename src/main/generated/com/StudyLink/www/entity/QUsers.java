package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUsers is a Querydsl query type for Users
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsers extends EntityPathBase<Users> {

    private static final long serialVersionUID = 1753229548L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUsers users = new QUsers("users");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath emailVerified = createBoolean("emailVerified");

    public final ListPath<Favorite, QFavorite> favoritedMentors = this.<Favorite, QFavorite>createList("favoritedMentors", Favorite.class, QFavorite.class, PathInits.DIRECT2);

    public final StringPath gradeYear = createString("gradeYear");

    public final StringPath interests = createString("interests");

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isStudentVerified = createBoolean("isStudentVerified");

    public final ListPath<MentorAvailability, QMentorAvailability> mentorAvailabilities = this.<MentorAvailability, QMentorAvailability>createList("mentorAvailabilities", MentorAvailability.class, QMentorAvailability.class, PathInits.DIRECT2);

    public final QMentorProfile mentorProfile;

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath oauthId = createString("oauthId");

    public final StringPath oauthProvider = createString("oauthProvider");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final StringPath role = createString("role");

    public final QStudentProfile studentProfile;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath username = createString("username");

    public QUsers(String variable) {
        this(Users.class, forVariable(variable), INITS);
    }

    public QUsers(Path<? extends Users> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUsers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUsers(PathMetadata metadata, PathInits inits) {
        this(Users.class, metadata, inits);
    }

    public QUsers(Class<? extends Users> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mentorProfile = inits.isInitialized("mentorProfile") ? new QMentorProfile(forProperty("mentorProfile"), inits.get("mentorProfile")) : null;
        this.studentProfile = inits.isInitialized("studentProfile") ? new QStudentProfile(forProperty("studentProfile"), inits.get("studentProfile")) : null;
    }

}

