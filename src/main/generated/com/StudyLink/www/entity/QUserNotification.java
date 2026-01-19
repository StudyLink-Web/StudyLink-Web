package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserNotification is a Querydsl query type for UserNotification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserNotification extends EntityPathBase<UserNotification> {

    private static final long serialVersionUID = -644620558L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserNotification userNotification = new QUserNotification("userNotification");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isEnabled = createBoolean("isEnabled");

    public final BooleanPath isRead = createBoolean("isRead");

    public final StringPath message = createString("message");

    public final StringPath notificationChannel = createString("notificationChannel");

    public final NumberPath<Long> notificationId = createNumber("notificationId", Long.class);

    public final StringPath notificationType = createString("notificationType");

    public final DateTimePath<java.time.LocalDateTime> readAt = createDateTime("readAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> relatedId = createNumber("relatedId", Long.class);

    public final QUsers user;

    public QUserNotification(String variable) {
        this(UserNotification.class, forVariable(variable), INITS);
    }

    public QUserNotification(Path<? extends UserNotification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserNotification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserNotification(PathMetadata metadata, PathInits inits) {
        this(UserNotification.class, metadata, inits);
    }

    public QUserNotification(Class<? extends UserNotification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

