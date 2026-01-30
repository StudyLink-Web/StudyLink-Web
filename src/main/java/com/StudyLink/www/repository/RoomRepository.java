package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByStatusAndIsPublic(Room.Status status, Boolean isPublic, Pageable pageable);

    Page<Room> findByStatusAndIsPublicAndMentorId(Room.Status status, Boolean isPublic, long mentorId, Pageable pageable);

    @Modifying
    @Query("UPDATE Room r SET r.status = :newStatus, r.mentorId = :userId, r.inProgressedAt = :now WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    int updateStatusIfPending(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("newStatus") Room.Status newStatus, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Room r WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    int deleteIfPending(@Param("roomId") long roomId);

    @Query("""
        SELECT r
        FROM Room r
        WHERE (r.mentorId = :userId OR r.studentId = :userId)
          AND (:status IS NULL OR r.status = :status)
          AND (:subjectId IS NULL OR r.subject.id = :subjectId)
          AND (:startDateTime IS NULL OR r.createdAt >= :startDateTime)
          AND (:endDateTime IS NULL OR r.createdAt <= :endDateTime)
    """)
    Page<Room> findByFilters(
            @Param("userId") Long userId,
            @Param("status") Room.Status status,
            @Param("subjectId") Integer subjectId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM rooms WHERE status = 'IN_PROGRESS' AND in_progressed_at <= NOW() - INTERVAL 20 MINUTE", nativeQuery = true)
    void deleteExpiredRooms();

    @Modifying
    @Query(value = "DELETE FROM rooms WHERE status IN ('TEMP', 'PENDING') AND in_progressed_at <= NOW() - INTERVAL 24 HOUR", nativeQuery = true)
    void deleteOldTempAndPendingRooms();

    @Query(value = "SELECT * FROM rooms r WHERE r.status = 'IN_PROGRESS' AND r.in_progressed_at <= NOW() - INTERVAL 20 MINUTE", nativeQuery = true)
    List<Room> findExpiredRooms();

    @Query(value = "SELECT * FROM rooms WHERE status IN ('TEMP', 'PENDING') AND in_progressed_at <= NOW() - INTERVAL 24 HOUR", nativeQuery = true)
    List<Room> findOldTempAndPendingRooms();
}
