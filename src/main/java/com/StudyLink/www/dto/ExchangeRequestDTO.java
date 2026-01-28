package com.StudyLink.www.dto;

import com.StudyLink.www.entity.ExchangeRequest;
import com.StudyLink.www.entity.ExchangeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestDTO {

    private Long id;

    private Long userId;

    private int point; // 환전 요청 금액

    private ExchangeStatus status; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt; // 요청일

    private LocalDateTime processedAt; // 처리일

    private String account; // 계좌번호

    private String bankName; // 은행명

    private String accountHolder; // 예금주

    private String rejectedReason; // 거부 사유

    private String transactionId; // PG사 거래 ID

    public ExchangeRequestDTO(ExchangeRequest exchangeRequest) {
        this.id = exchangeRequest.getId();
        this.userId = exchangeRequest.getUser().getUserId();
        this.point = exchangeRequest.getPoint();
        this.status = exchangeRequest.getStatus();
        this.createdAt = exchangeRequest.getCreatedAt();
        this.processedAt = exchangeRequest.getProcessedAt();
        this.account = exchangeRequest.getAccount();
        this.bankName = exchangeRequest.getBankName();
        this.accountHolder = exchangeRequest.getAccountHolder();
        this.rejectedReason = exchangeRequest.getRejectedReason();
        this.transactionId = exchangeRequest.getTransactionId();
    }
}