package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByStatusAndIsPublic(Room.Status status, Boolean isPublic, Pageable pageable);

    Page<Room> findByStatusAndIsPublicAndMentorId(Room.Status status, Boolean isPublic, long mentorId, Pageable pageable);

    @Modifying
    @Query("UPDATE Room r SET r.status = :newStatus WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    int updateStatusIfPending(@Param("roomId") Long roomId, @Param("newStatus") Room.Status newStatus);

    @Modifying
    @Query("DELETE FROM Room r WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    int deleteIfPending(@Param("roomId") long roomId);

    Page<Room> findByStudentIdOrMentorId(long userId, long userId1, Pageable pageable);
}
