package com.StudyLink.www.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoomFile is a Querydsl query type for RoomFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoomFile extends EntityPathBase<RoomFile> {

    private static final long serialVersionUID = -543895661L;

    public static final QRoomFile roomFile = new QRoomFile("roomFile");

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Integer> fileType = createNumber("fileType", Integer.class);

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    public final StringPath saveDir = createString("saveDir");

    public final StringPath uuid = createString("uuid");

    public QRoomFile(String variable) {
        super(RoomFile.class, forVariable(variable));
    }

    public QRoomFile(Path<? extends RoomFile> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoomFile(PathMetadata metadata) {
        super(RoomFile.class, metadata);
    }

}

