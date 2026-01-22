package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPushToken is a Querydsl query type for PushToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPushToken extends EntityPathBase<PushToken> {

    private static final long serialVersionUID = 630981603L;

    public static final QPushToken pushToken = new QPushToken("pushToken");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastUpdated = createDateTime("lastUpdated", java.time.LocalDateTime.class);

    public final StringPath token = createString("token");

    public final StringPath username = createString("username");

    public QPushToken(String variable) {
        super(PushToken.class, forVariable(variable));
    }

    public QPushToken(Path<? extends PushToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPushToken(PathMetadata metadata) {
        super(PushToken.class, metadata);
    }

}

