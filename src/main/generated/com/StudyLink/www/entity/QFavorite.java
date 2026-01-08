package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFavorite is a Querydsl query type for Favorite
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavorite extends EntityPathBase<Favorite> {

    private static final long serialVersionUID = 680629304L;

    public static final QFavorite favorite = new QFavorite("favorite");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> favoriteId = createNumber("favoriteId", Long.class);

    public final NumberPath<Long> mentorId = createNumber("mentorId", Long.class);

    public final NumberPath<Long> studentId = createNumber("studentId", Long.class);

    public QFavorite(String variable) {
        super(Favorite.class, forVariable(variable));
    }

    public QFavorite(Path<? extends Favorite> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFavorite(PathMetadata metadata) {
        super(Favorite.class, metadata);
    }

}

