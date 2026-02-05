package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserSettings (사용자 설정)
 * Users와 1:1 관계
 * 테마, 언어, 알림 등 사용자 설정 정보
 */
@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;

    /**
     * 테마 설정
     * LIGHT: 라이트 모드
     * DARK: 다크 모드
     */
    @Column(name = "theme_mode", length = 20, nullable = false)
    private String themeMode = "LIGHT";

    /**
     * 언어 설정
     * KO: 한국어
     * EN: 영어
     * JA: 일본어
     */
    @Column(name = "language", length = 10, nullable = false)
    private String language = "KO";

    /**
     * 알림 활성화 여부 (전체)
     */
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    /**
     * 이메일 알림 활성화
     */
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;

    /**
     * 푸시 알림 활성화
     */
    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications = true;

    /**
     * SMS 알림 활성화
     */
    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications = false;

    /**
     * 마케팅 정보 수신 동의
     */
    @Column(name = "marketing_agree", nullable = false)
    private Boolean marketingAgree = false;

    /**
     * 개인정보 처리방침 동의
     */
    @Column(name = "privacy_policy_agree", nullable = false)
    private Boolean privacyPolicyAgree = true;

    /**
     * 서비스 이용약관 동의
     */
    @Column(name = "terms_of_service_agree", nullable = false)
    private Boolean termsOfServiceAgree = true;

    /**
     * 공개 프로필 여부
     */
    @Column(name = "profile_public", nullable = false)
    private Boolean profilePublic = true;

    /**
     * 마지막 접속 시간
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.themeMode == null)
            this.themeMode = "LIGHT";
        if (this.language == null)
            this.language = "KO";
        if (this.notificationsEnabled == null)
            this.notificationsEnabled = true;
        if (this.emailNotifications == null)
            this.emailNotifications = true;
        if (this.pushNotifications == null)
            this.pushNotifications = true;
        if (this.smsNotifications == null)
            this.smsNotifications = false;
        if (this.marketingAgree == null)
            this.marketingAgree = false;
        if (this.privacyPolicyAgree == null)
            this.privacyPolicyAgree = true;
        if (this.termsOfServiceAgree == null)
            this.termsOfServiceAgree = true;
        if (this.profilePublic == null)
            this.profilePublic = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
