package com.StudyLink.www.entity;

public enum PaymentStatus {
    PENDING("대기"),
    REQUESTED("결제 요청"),
    APPROVED("결제 완료"),
    FAILED("결제 실패"),
    CANCELED("결제 취소");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}