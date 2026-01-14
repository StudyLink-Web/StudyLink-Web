package com.StudyLink.www.entity;

import com.StudyLink.www.dto.FavoriteDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Favorite (멘토 즐겨찾기, 찜)
 * 학생이 멘토를 즐겨찾기하는 관계
 */
@Entity
@Table(name = "favorite", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "mentor_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    /**
     * 학생 ID
     * FK: Users.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    /**
     * 멘토 ID
     * FK: Users.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Users mentor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 편의 메서드: 학생 ID 조회
     */
    public Long getStudentId() {
        return this.student != null ? this.student.getUserId() : null;
    }

    /**
     * 편의 메서드: 멘토 ID 조회
     */
    public Long getMentorId() {
        return this.mentor != null ? this.mentor.getUserId() : null;
    }

    /**
     * DTO를 받아 엔티티 생성
     */
    public Favorite(FavoriteDTO dto, Users student, Users mentor) {
        this.favoriteId = dto.getFavoriteId();
        this.student = student; // Users 엔티티
        this.mentor = mentor;   // Users 엔티티
        this.createdAt = LocalDateTime.now();
    }
}
