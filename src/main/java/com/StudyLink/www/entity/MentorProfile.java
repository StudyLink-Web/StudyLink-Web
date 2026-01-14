package com.StudyLink.www.entity;

import com.StudyLink.www.dto.MentorProfileDTO;
import com.StudyLink.www.dto.UsersDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mentor_Profile (멘토 상세 - 대학생)
 * Users와 1:1 관계
 */
@Entity
@Table(name = "mentor_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;


    /**
     * 재학 중인 대학 ID
     * FK: Universities.univ_id
     */
    @Column(name = "univ_id")
    private Long univId;

    /**
     * 재학 중인 학과 ID
     * FK: Departments.dept_id
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * 학생증 인증 이미지 경로
     */
    @Column(length = 255)
    private String studentCardImg;

    /**
     * 인증 여부
     * true: 인증 완료, false: 미인증
     */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    /**
     * 멘토 한줄 소개
     * 예: "서울대 합격 노하우 공유합니다"
     */
    @Column(length = 500)
    private String introduction;

    /**
     * 평점 (선택사항)
     * 범위: 1.0 ~ 5.0
     */
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    /**
     * 포인트 (현금으로 출금 가능)
     */
    @Column(name = "point", nullable = false)
    private Long point = 0L;

    /**
     * 경험치
     * 레벨 표시, 랭킹 구하기 등에 사용
     */
    @Column(name = "exp", nullable = false)
    private Long exp = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.point == null) this.point = 0L;
        if (this.exp == null) this.exp = 0L;
        if (this.averageRating == null) this.averageRating = 0.0;
        if (this.isVerified == null) this.isVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🔹 DTO -> 엔티티 생성자
    public MentorProfile(MentorProfileDTO dto, Users user) {
        this.user = user;
        this.userId = user.getUserId(); // @MapsId 필요
        this.univId = dto.getUnivId();
        this.deptId = dto.getDeptId();
        this.studentCardImg = dto.getStudentCardImg();
        this.isVerified = dto.getIsVerified() != null ? dto.getIsVerified() : false;
        this.introduction = dto.getIntroduction();
        this.averageRating = dto.getAverageRating() != null ? dto.getAverageRating() : 0.0;
        this.point = dto.getPoint() != null ? dto.getPoint() : 0L;
        this.exp = dto.getExp() != null ? dto.getExp() : 0L;
        this.createdAt = dto.getCreatedAt();
        this.updatedAt = dto.getUpdatedAt();
    }
}
