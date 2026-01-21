package com.StudyLink.www.entity;

/**
 * 사용자 역할 Enum
 * STUDENT: 학생
 * MENTOR: 멘토
 * ADMIN: 관리자
 */
public enum Role {
    STUDENT("학생"),
    MENTOR("멘토"),
    ADMIN("관리자");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // String -> Enum 변환 헬퍼 메서드
    public static Role fromString(String value) {
        if (value == null) return STUDENT;
        try {
            return Role.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT;  // 기본값
        }
    }
}
