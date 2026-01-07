package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_username", columnList = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // ========== 기본 정보 ==========
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String nickname;

    // ========== 추가 정보 ==========
    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String gradeYear;

    @Column(length = 255)
    private String interests;

    // ========== 역할 및 인증 ==========
    @Column(nullable = false, length = 50)
    private String role;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    // ========== OAuth 정보 ==========
    @Column(length = 50)
    private String oauthProvider;

    private String oauthId;

    private String profileImageUrl;

    // ========== 타임스탬프 ==========
    // 여기 수정 @CreationTimestamp 추가
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 여기 수정 @UpdateTimestamp 추가
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
