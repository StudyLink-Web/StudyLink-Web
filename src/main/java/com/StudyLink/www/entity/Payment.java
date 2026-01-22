package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"payment_key"}),
                @UniqueConstraint(columnNames = {"order_id"})
        }
)
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {
    /** 내부 결제 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    /** 가맹점 주문 번호 */
    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    /** 토스 결제 고유 키 */
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    /** 결제 유저 */
    @Column(name = "user_id", nullable = false)
    private long userId;

    /** 결제 상품 id */
    @Column(name = "product_id", nullable = false)
    private int productId;

    /** 결제 금액 (KRW 기준, 원 단위) */
    @Column(name = "amount", nullable = false)
    private int amount;

    /** 결제 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    /** 결제 수단 (CARD, EASY_PAY, TRANSFER 등) */
    @Column(name = "method", length = 30)
    private String method;

    /** 결제 요청 시각 */
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    /** 결제 승인 시각 */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** 통화 */
    @Column(name = "currency", length = 10)
    private String currency;
}
