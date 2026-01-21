package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lesson (수업)
 * MentorProfile과 Users(학생) 간의 수업 정보
 */
@Entity
@Table(name = "lesson")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    /**
     * 멘토 FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    /**
     * 학생 FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    /**
     * 수업 상태
     * "pending" (대기), "confirmed" (확정), "completed" (완료), "cancelled" (취소)
     */
    @Column(name = "status", length = 50, nullable = false)
    private String status = "pending";

    /**
     * 수업 시작 시간
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 수업 종료 시간
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 수업 방식
     * "online", "offline"
     */
    @Column(name = "lesson_type", length = 50)
    private String lessonType;

    /**
     * 수업 위치 (오프라인인 경우)
     */
    @Column(name = "lesson_location", length = 255)
    private String lessonLocation;

    /**
     * 수업료
     */
    @Column(name = "price")
    private Integer price;

    /**
     * 수업 메모/설명
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
