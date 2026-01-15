package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatusAndIsPublic(Room.Status status, Boolean isPublic);

    List<Room> findByStatusAndIsPublicAndMentorId(Room.Status status, Boolean isPublic, long mentorId);
}
