package com.StudyLink.www.repository;

import com.StudyLink.www.entity.ExchangeRequest;
import com.StudyLink.www.entity.ExchangeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    @Query("""
        SELECT e
        FROM ExchangeRequest e
        JOIN e.user u
        WHERE (:status IS NULL OR e.status = :status)
        AND (:email IS NULL OR u.email LIKE %:email%)
        AND (:startDateTime IS NULL OR e.createdAt >= :startDateTime)
        AND (:endDatePlus IS NULL OR e.createdAt < :endDatePlus)
    """)
    Page<ExchangeRequest> searchByCreatedAt(
            @Param("status") ExchangeStatus status,
            @Param("email") String email,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDatePlus") LocalDateTime endDatePlus,
            Pageable pageable
    );


    @Query("""
        SELECT e
        FROM ExchangeRequest e
        JOIN e.user u
        WHERE (:status IS NULL OR e.status = :status)
        AND (:email IS NULL OR u.email LIKE %:email%)
        AND (:startDateTime IS NULL OR e.processedAt >= :startDateTime)
        AND (:endDatePlus IS NULL OR e.processedAt < :endDatePlus)
    """)
    Page<ExchangeRequest> searchByProcessedAt(
            @Param("status") ExchangeStatus status,
            @Param("email") String email,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDatePlus") LocalDateTime endDatePlus,
            Pageable pageable
    );

    int countByStatusAndCreatedAtBetween(ExchangeStatus exchangeStatus, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT SUM(e.point)
        FROM ExchangeRequest e
        WHERE (:status IS NULL OR e.status = :status)
          AND e.createdAt BETWEEN :start AND :end
    """)
    Long sumAmountByCreatedAtBetween(
            @Param("status") ExchangeStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        select coalesce(sum(e.point), 0)
        from ExchangeRequest e
        where e.processedAt < :start
        and e.status = :approvedStatus
    """)
    long sumAmountBefore(
            @Param("start") LocalDateTime start,
            @Param("approvedStatus") ExchangeStatus approvedStatus
    );

    @Query("""
        select coalesce(sum(e.point), 0)
        from ExchangeRequest e
        where e.processedAt >= :start
        and e.processedAt < :end
        and e.status = :approvedStatus
    """)
    long sumAmountByDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("approvedStatus") ExchangeStatus approvedStatus
    );
}
