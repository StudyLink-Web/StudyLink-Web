package com.StudyLink.www.dto;

public record VerifyEmailChangeResult(
        boolean success,
        String reason,
        String maskedEmail,
        boolean isUniversityEmail
) {}