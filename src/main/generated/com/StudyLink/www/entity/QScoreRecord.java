package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScoreRecord is a Querydsl query type for ScoreRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScoreRecord extends EntityPathBase<ScoreRecord> {

    private static final long serialVersionUID = -815742617L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScoreRecord scoreRecord = new QScoreRecord("scoreRecord");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final ListPath<StudentScore, QStudentScore> scores = this.<StudentScore, QStudentScore>createList("scores", StudentScore.class, QStudentScore.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final QUsers user;

    public QScoreRecord(String variable) {
        this(ScoreRecord.class, forVariable(variable), INITS);
    }

    public QScoreRecord(Path<? extends ScoreRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScoreRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScoreRecord(PathMetadata metadata, PathInits inits) {
        this(ScoreRecord.class, metadata, inits);
    }

    public QScoreRecord(Class<? extends ScoreRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

