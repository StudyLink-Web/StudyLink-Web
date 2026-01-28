package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentDTO {
    private Long paymentId;
    private String orderId;
    private String paymentKey;

    private Long userId; // Users 엔티티에서 userId만 추출해서 담음

    private int productId;

    private int amount;
    private PaymentStatus status;
    private String method;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime failedAt;
    private LocalDateTime canceledAt;

    private String currency;

    public PaymentDTO(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrderId();
        this.paymentKey = payment.getPaymentKey();
        this.userId = payment.getUser() != null ? payment.getUser().getUserId() : null;
        this.productId = payment.getProductId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus();
        this.method = payment.getMethod();
        this.requestedAt = payment.getRequestedAt();
        this.approvedAt = payment.getApprovedAt();
        this.failedAt = payment.getFailedAt();
        this.canceledAt = payment.getCanceledAt();
        this.currency = payment.getCurrency();
    }
}
