package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentVerificationDTO {
    private Long userId;
    private String schoolEmail;
    private Boolean isVerifiedStudent;
    private LocalDateTime verifiedAt;
    private String verificationStatus;  // "PENDING", "VERIFIED", "FAILED"
    private String message;
}