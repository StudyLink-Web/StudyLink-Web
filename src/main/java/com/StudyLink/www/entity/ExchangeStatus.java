package com.StudyLink.www.entity;

public enum ExchangeStatus {
    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("거부");

    private final String displayName;

    ExchangeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}