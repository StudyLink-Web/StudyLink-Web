package com.StudyLink.www.entity;

import com.StudyLink.www.dto.StudentProfileDTO;
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
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(length = 100)
    private String targetUniversity;

    @Column(length = 100)
    private String targetMajor;

    @Column(length = 50)
    private String regionPreference;

    @Column(columnDefinition = "int default 0")
    private int chargedPoint;

    @Column(columnDefinition = "int default 0")
    private int bonusPoint;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🔹 DTO -> 엔티티 생성자
    public StudentProfile(StudentProfileDTO studentProfileDTO, Users users) {
        this.userId = studentProfileDTO.getUserId();
        this.user = users;
        this.targetUniversity = studentProfileDTO.getTargetUniversity();
        this.targetMajor = studentProfileDTO.getTargetMajor();
        this.regionPreference = studentProfileDTO.getRegionPreference();
        this.chargedPoint = studentProfileDTO.getChargedPoint();
        this.bonusPoint = studentProfileDTO.getBonusPoint();
        this.createdAt = studentProfileDTO.getCreatedAt();
        this.updatedAt = studentProfileDTO.getUpdatedAt();
    }
}
