package com.StudyLink.www.repository;

import com.StudyLink.www.entity.ExchangeRequest;
import com.StudyLink.www.entity.ExchangeStatus;
import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment findByOrderId(String orderId);

    @Query("""
        SELECT p
        FROM Payment p
        JOIN p.user u
        WHERE (:status IS NULL OR p.status = :status)
        AND (:method IS NULL OR p.method = :method)
        AND (:email IS NULL OR u.email LIKE %:email%)
        AND (:startDateTime IS NULL OR p.approvedAt >= :startDateTime)
        AND (:endDatePlus IS NULL OR p.approvedAt < :endDatePlus)
    """)
    Page<Payment> searchPayments(
            @Param("status") PaymentStatus status,
            @Param("method") String method,
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
}
