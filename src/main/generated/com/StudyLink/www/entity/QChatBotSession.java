package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatBotSession is a Querydsl query type for ChatBotSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatBotSession extends EntityPathBase<ChatBotSession> {

    private static final long serialVersionUID = 836330979L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatBotSession chatBotSession = new QChatBotSession("chatBotSession");

    public final QTimeBase _super = new QTimeBase(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final ListPath<ChatBotMessage, QChatBotMessage> messages = this.<ChatBotMessage, QChatBotMessage>createList("messages", ChatBotMessage.class, QChatBotMessage.class, PathInits.DIRECT2);

    public final NumberPath<Long> sessionId = createNumber("sessionId", Long.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUsers user;

    public QChatBotSession(String variable) {
        this(ChatBotSession.class, forVariable(variable), INITS);
    }

    public QChatBotSession(Path<? extends ChatBotSession> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatBotSession(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatBotSession(PathMetadata metadata, PathInits inits) {
        this(ChatBotSession.class, metadata, inits);
    }

    public QChatBotSession(Class<? extends ChatBotSession> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUsers(forProperty("user"), inits.get("user")) : null;
    }

}

