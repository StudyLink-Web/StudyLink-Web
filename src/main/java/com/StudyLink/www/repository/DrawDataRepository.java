package com.StudyLink.www.repository;

import com.StudyLink.www.entity.DrawData;
import com.StudyLink.www.entity.UndoRedoStack;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DrawDataRepository extends MongoRepository<DrawData, String> {
    List<DrawData> findByRoomId(long roomId);

    void deleteByRoomIdAndUuid(long roomId, String uuid);

    void deleteByRoomId(long roomId);
}
