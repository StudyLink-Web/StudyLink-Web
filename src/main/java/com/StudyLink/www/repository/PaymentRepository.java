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

import java.time.LocalDate;
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

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.approvedAt >= :start AND p.approvedAt < :end")
    int getTodayPaymentCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.approvedAt >= :start AND p.approvedAt < :end")
    Long getSumPaymentAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        where p.requestedAt < :start
    """)
    long sumAmountBefore(@Param("start") LocalDateTime start);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        where p.status = 'APPROVED'
        and p.requestedAt >= :start
        and p.requestedAt < :end
    """)
    long sumAmountByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
