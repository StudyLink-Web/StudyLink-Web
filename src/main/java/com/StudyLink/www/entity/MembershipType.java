package com.StudyLink.www.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipType {
    FREE("Free"),
    STANDARD("Standard"),
    PREMIUM("Premium PASS");

    private final String displayName;

    public static MembershipType fromString(String text) {
        for (MembershipType b : MembershipType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return FREE;
    }
}
