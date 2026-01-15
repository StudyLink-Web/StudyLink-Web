package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudentScore is a Querydsl query type for StudentScore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudentScore extends EntityPathBase<StudentScore> {

    private static final long serialVersionUID = 2143425971L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudentScore studentScore = new QStudentScore("studentScore");

    public final StringPath category = createString("category");

    public final StringPath optionalSubject = createString("optionalSubject");

    public final NumberPath<Double> score = createNumber("score", Double.class);

    public final NumberPath<Long> scoreId = createNumber("scoreId", Long.class);

    public final QScoreRecord scoreRecord;

    public final StringPath scoreType = createString("scoreType");

    public final StringPath subjectName = createString("subjectName");

    public final QUsers user;

    public QStudentScore(String variable) {
        this(StudentScore.class, forVariable(variable), INITS);
    }

    public QStudentScore(Path<? extends StudentScore> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudentScore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudentScore(PathMetadata metadata, PathInits inits) {
        this(StudentScore.class, metadata, inits);
    }

    public QStudentScore(Class<? extends StudentScore> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.scoreRecord = inits.isInitialized("scoreRecord") ? new QScoreRecord(forProperty("scoreRecord"), inits.get("scoreRecord")) : null;
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

