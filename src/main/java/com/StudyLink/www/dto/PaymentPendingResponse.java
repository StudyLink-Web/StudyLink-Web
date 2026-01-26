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
    private int productPrice;
    private String currency;
}
