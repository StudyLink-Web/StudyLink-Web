package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoverLetter is a Querydsl query type for CoverLetter
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoverLetter extends EntityPathBase<CoverLetter> {

    private static final long serialVersionUID = 1634885281L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoverLetter coverLetter = new QCoverLetter("coverLetter");

    public final QTimeBase _super = new QTimeBase(this);

    public final StringPath content = createString("content");

    public final NumberPath<Long> coverLetterId = createNumber("coverLetterId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> questionNum = createNumber("questionNum", Integer.class);

    public final StringPath questionText = createString("questionText");

    public final StringPath status = createString("status");

    public final StringPath targetMajor = createString("targetMajor");

    public final StringPath targetUniversity = createString("targetUniversity");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUsers user;

    public QCoverLetter(String variable) {
        this(CoverLetter.class, forVariable(variable), INITS);
    }

    public QCoverLetter(Path<? extends CoverLetter> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoverLetter(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoverLetter(PathMetadata metadata, PathInits inits) {
        this(CoverLetter.class, metadata, inits);
    }

    public QCoverLetter(Class<? extends CoverLetter> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

