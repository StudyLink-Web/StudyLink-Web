package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 인증 토큰
 * 계정 생성 시 이메일 인증을 위한 임시 토큰 저장
 *
 * 특징:
 * - 이메일 + 요청한 계정명으로 저장 (중복 인증 방지)
 * - 10분 유효
 * - 인증 완료 후 자동 삭제
 */
@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_email_username", columnList = "email,requested_username"),
        @Index(name = "idx_requested_username", columnList = "requested_username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    /**
     * 인증받을 이메일 주소
     * 예: user@ewha.ac.kr
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * ⭐ 중요: 이메일을 요청한 계정명
     * 예: "accountA", "accountC"
     * 같은 이메일로 여러 계정이 요청해도 독립적으로 관리 가능
     */
    @Column(name = "requested_username", nullable = false, length = 50)
    private String requestedUsername;

    /**
     * 6자리 인증 코드
     * 예: "123456"
     */
    @Column(name = "verification_code", nullable = false, length = 10)
    private String verificationCode;

    /**
     * 토큰 생성 시간
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 시간 (생성 후 10분)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 만료 여부 확인
     * @return true: 만료됨, false: 유효함
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰 유효 여부 확인
     * @return true: 유효함, false: 만료되었거나 무효함
     */
    public boolean isValid() {
        return !isExpired();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiresAt == null) {
            // 10분 후 만료
            this.expiresAt = LocalDateTime.now().plusMinutes(10);
        }
    }
}
