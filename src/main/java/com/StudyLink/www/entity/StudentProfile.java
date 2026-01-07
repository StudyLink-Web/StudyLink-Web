package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;  // ✅ profile_id → profileId

    // ========== Users 테이블과 1:1 관계 ==========
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Users user;

    // ========== 학생 정보 ==========
    @Column(name = "target_university", length = 100)
    private String targetUniversity;  // ✅ target_university → targetUniversity

    @Column(name = "target_major", length = 100)
    private String targetMajor;  // ✅ target_major → targetMajor

    @Column(name = "region_preference", length = 50)
    private String regionPreference;  // ✅ region_preference → regionPreference

    // ========== 타임스탬프 ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // ✅ created_at → createdAt

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // ✅ updated_at → updatedAt
}
