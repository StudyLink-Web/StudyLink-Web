package com.StudyLink.www.repository;

import com.StudyLink.www.entity.UndoRedoStack;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UndoRedoStackRepository extends MongoRepository<UndoRedoStack, String> {

    Optional<UndoRedoStack> findByRoomId(long roomId);
}
