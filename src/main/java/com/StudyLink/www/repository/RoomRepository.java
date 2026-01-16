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
    @Query("UPDATE Room r SET r.status = :newStatus, r.mentorId = :userId WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    int updateStatusIfPending(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("newStatus") Room.Status newStatus);

    @Modifying
    @Query("DELETE FROM Room r WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    int deleteIfPending(@Param("roomId") long roomId);

    @Query("""
    SELECT r 
    FROM Room r 
    WHERE r.studentId = :userId OR r.mentorId = :userId
    ORDER BY 
        CASE r.status
            WHEN 'TEMP' THEN 1
            WHEN 'PENDING' THEN 2
            WHEN 'IN_PROGRESS' THEN 3
            WHEN 'ANSWERED' THEN 4
            WHEN 'COMPLETED' THEN 5
            ELSE 99
        END,
        r.createdAt DESC
""")
    Page<Room> findByStudentOrMentorOrderByStatus(@Param("userId") long userId, Pageable pageable);
}
