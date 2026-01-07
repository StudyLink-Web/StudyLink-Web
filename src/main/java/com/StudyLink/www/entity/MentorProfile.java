package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;  // ✅ profile_id → profileId

    // ========== Users 테이블과 1:1 관계 ==========
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Users user;

    // ========== 대학 및 학과 정보 ==========
    @Column(name = "univ_id")
    private Long univId;  // ✅ univ_id → univId

    @Column(name = "dept_id")
    private Long deptId;  // ✅ dept_id → deptId

    // ========== 인증 정보 ==========
    @Column(name = "student_card_img", length = 500)
    private String studentCardImg;  // ✅ student_card_img → studentCardImg

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;  // ✅ is_verified → isVerified

    // ========== 멘토 정보 ==========
    @Column(name = "introduction", length = 255)
    private String introduction;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;  // ✅ average_rating → averageRating

    @Column(name = "point", nullable = false)
    private Integer point = 0;

    // ========== 타임스탬프 ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // ✅ created_at → createdAt

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // ✅ updated_at → updatedAt
}
