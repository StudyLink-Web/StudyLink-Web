package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;  // ✅ user_id → userId

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, unique = true)
    private String nickname;

    @Column(length = 20, nullable = false)
    private String role; // 'STUDENT', 'MENTOR', 'ADMIN'

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // ✅ created_at → createdAt

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();  // ✅ updated_at → updatedAt

    // ========== 관계설정 (Phase 2에서 사용) ==========
    // =================== 임시 주석 ===================
    /*
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private StudentProfile studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private MentorProfile mentorProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private java.util.List mentorAvailabilities;
    */
    // =============================================
}
