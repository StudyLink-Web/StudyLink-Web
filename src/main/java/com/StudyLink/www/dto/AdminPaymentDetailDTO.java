package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminPaymentDetailDTO {
    private PaymentDTO paymentDTO;
    private ProductDTO productDTO;
    private UsersDTO usersDTO;
}
