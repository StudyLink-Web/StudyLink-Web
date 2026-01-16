package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSettings is a Querydsl query type for UserSettings
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSettings extends EntityPathBase<UserSettings> {

    private static final long serialVersionUID = -1454494294L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSettings userSettings = new QUserSettings("userSettings");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath emailNotifications = createBoolean("emailNotifications");

    public final StringPath language = createString("language");

    public final DateTimePath<java.time.LocalDateTime> lastLoginAt = createDateTime("lastLoginAt", java.time.LocalDateTime.class);

    public final BooleanPath marketingAgree = createBoolean("marketingAgree");

    public final BooleanPath notificationsEnabled = createBoolean("notificationsEnabled");

    public final BooleanPath privacyPolicyAgree = createBoolean("privacyPolicyAgree");

    public final BooleanPath profilePublic = createBoolean("profilePublic");

    public final BooleanPath pushNotifications = createBoolean("pushNotifications");

    public final BooleanPath smsNotifications = createBoolean("smsNotifications");

    public final BooleanPath termsOfServiceAgree = createBoolean("termsOfServiceAgree");

    public final StringPath themeMode = createString("themeMode");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUsers user;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserSettings(String variable) {
        this(UserSettings.class, forVariable(variable), INITS);
    }

    public QUserSettings(Path<? extends UserSettings> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSettings(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSettings(PathMetadata metadata, PathInits inits) {
        this(UserSettings.class, metadata, inits);
    }

    public QUserSettings(Class<? extends UserSettings> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

