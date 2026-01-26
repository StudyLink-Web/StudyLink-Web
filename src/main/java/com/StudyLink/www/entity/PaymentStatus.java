package com.StudyLink.www.entity;


public enum PaymentStatus {
    PENDING("대기"),
    REQUESTED("요청"),
    APPROVED("완료"),
    FAILED("실패"),
    CANCELED("취소");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}