package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByStatusAndIsPublic(Room.Status status, Boolean isPublic, Pageable pageable);

    Page<Room> findByStatusAndIsPublicAndMentorId(Room.Status status, Boolean isPublic, long mentorId, Pageable pageable);
}
