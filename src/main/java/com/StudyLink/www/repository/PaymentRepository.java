package com.StudyLink.www.repository;

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
    Page<Payment> search(
            @Param("status") PaymentStatus status,
            @Param("method") String method,
            @Param("email") String email,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDatePlus") LocalDateTime endDatePlus,
            Pageable pageable
    );
}
