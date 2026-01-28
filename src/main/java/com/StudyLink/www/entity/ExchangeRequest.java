package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_request")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private int point; // 환전 요청 금액

    @Enumerated(EnumType.STRING)  // DB에 문자열로 저장
    @Column(nullable = false, length = 20)
    private ExchangeStatus status;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt; // 요청일

    @Column()
    private LocalDateTime processedAt; // 처리일

    @Column(nullable = false, length = 50)
    private String account; // 계좌번호

    @Column(nullable = false, length = 20)
    private String bankName; // 은행명

    @Column(nullable = false, length = 50)
    private String accountHolder; // 예금주

    @Column(length = 200)
    private String rejectedReason;

    @Column(length = 200)
    private String transactionId; // PG사 거래 ID
}
