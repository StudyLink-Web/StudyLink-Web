package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubject is a Querydsl query type for Subject
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubject extends EntityPathBase<Subject> {

    private static final long serialVersionUID = -494352624L;

    public static final QSubject subject = new QSubject("subject");

    public final StringPath color = createString("color");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> subjectId = createNumber("subjectId", Integer.class);

    public QSubject(String variable) {
        super(Subject.class, forVariable(variable));
    }

    public QSubject(Path<? extends Subject> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubject(PathMetadata metadata) {
        super(Subject.class, metadata);
    }

}

