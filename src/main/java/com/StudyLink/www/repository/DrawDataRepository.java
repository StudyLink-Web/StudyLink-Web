package com.StudyLink.www.repository;

import com.StudyLink.www.entity.DrawData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DrawDataRepository extends MongoRepository<DrawData, String> {
    List<DrawData> findByRoomId(long roomId);

    void deleteByRoomIdAndUuid(long roomId, String uuid);
}
