package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatBotMessage is a Querydsl query type for ChatBotMessage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatBotMessage extends EntityPathBase<ChatBotMessage> {

    private static final long serialVersionUID = -193731756L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatBotMessage chatBotMessage = new QChatBotMessage("chatBotMessage");

    public final QTimeBase _super = new QTimeBase(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> messageId = createNumber("messageId", Long.class);

    public final StringPath role = createString("role");

    public final QChatBotSession session;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatBotMessage(String variable) {
        this(ChatBotMessage.class, forVariable(variable), INITS);
    }

    public QChatBotMessage(Path<? extends ChatBotMessage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatBotMessage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatBotMessage(PathMetadata metadata, PathInits inits) {
        this(ChatBotMessage.class, metadata, inits);
    }

    public QChatBotMessage(Class<? extends ChatBotMessage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.session = inits.isInitialized("session") ? new QChatBotSession(forProperty("session"), inits.get("session")) : null;
    }

}

