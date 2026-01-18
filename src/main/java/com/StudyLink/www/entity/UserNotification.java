package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserNotification (사용자 알림 설정)
 * Users와 1:N 관계
 * 알림 유형별 활성화/비활성화 설정
 */
@Entity
@Table(name = "user_notification",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_notification_type", columnList = "notification_type")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * 알림 유형
     * ANSWER_RECEIVED: 답변 받음
     * COMMENT_RECEIVED: 댓글 받음
     * FOLLOW_RECEIVED: 팔로우 받음
     * QUESTION_ANSWERED: 질문에 답변이 달림
     * PAYMENT_COMPLETED: 결제 완료
     * WITHDRAWAL_COMPLETED: 환급 완료
     * SYSTEM: 시스템 공지
     */
    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType;

    /**
     * 해당 알림 유형 활성화 여부
     */
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    /**
     * 알림 채널 (이메일/푸시/SMS)
     */
    @Column(name = "notification_channel", length = 20)
    private String notificationChannel; // EMAIL, PUSH, SMS

    /**
     * 알림 메시지 (저장용)
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * 읽음 여부
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * 관련 엔티티 ID (질문 ID, 답변 ID 등)
     */
    @Column(name = "related_id")
    private Long relatedId;

    /**
     * 생성 시간
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 읽은 시간
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isEnabled == null) this.isEnabled = true;
        if (this.isRead == null) this.isRead = false;
    }
}
