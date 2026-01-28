package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminPaymentDTO {
    private PaymentDTO paymentDTO;
    private String productName;
    private String email;
}
