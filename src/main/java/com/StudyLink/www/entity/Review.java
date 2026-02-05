package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review (리뷰)
 * 학생이 멘토에 대해 작성하는 리뷰
 */
@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    /**
     * 멘토 FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    /**
     * 학생(작성자) FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    /**
     * 수업 FK (선택사항)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    /**
     * 별점 (1.0 ~ 5.0)
     */
    @Column(name = "rating", nullable = false)
    private Double rating;

    /**
     * 리뷰 내용
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 공개 여부
     */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isPublic == null)
            this.isPublic = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
