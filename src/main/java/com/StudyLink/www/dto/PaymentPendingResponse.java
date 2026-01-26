package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPendingResponse {
    private String orderId;
    private String productName;
    private String productDescription; // 📍 추가: 상품 상세 설명
    private int productPrice;
    private String currency;
    private String customerKey; // 📍 추가: 토스 결제 필수 키
}
