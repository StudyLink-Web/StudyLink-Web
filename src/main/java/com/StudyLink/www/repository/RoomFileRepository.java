package com.StudyLink.www.repository;

import com.StudyLink.www.dto.RoomFileDTO;
import com.StudyLink.www.entity.RoomFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomFileRepository extends JpaRepository<RoomFile, String> {

    void deleteByRoomId(long roomId);

    List<RoomFile> findByRoomId(long roomId);
}
