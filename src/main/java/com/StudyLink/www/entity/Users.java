/* entity/Users */

package com.StudyLink.www.entity;

import com.StudyLink.www.dto.UsersDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "school_email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role = Role.STUDENT;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "is_student_verified", nullable = false)
    private Boolean isStudentVerified = false;

    @Column(name = "school_email", unique = true)
    private String schoolEmail;

    @Column(name = "is_verified_student", nullable = false)
    private Boolean isVerifiedStudent = false;

    @Column(name = "school_email_verification_token")
    private String schoolEmailVerificationToken;

    @Column(name = "school_email_token_expires")
    private LocalDateTime schoolEmailTokenExpires;

    @Column(name = "school_email_verified_at")
    private LocalDateTime schoolEmailVerifiedAt;

    @Column(name = "last_email_sent_at")
    private LocalDateTime lastEmailSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String oauthProvider;

    @Column(length = 100)
    private String oauthId;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column
    private String gradeYear;

    @Column
    private String interests;

    @Column
    private String phone;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private StudentProfile studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private MentorProfile mentorProfile;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MentorAvailability> mentorAvailabilities;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favoritedMentors;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.emailVerified == null)
            this.emailVerified = false;
        if (this.isStudentVerified == null)
            this.isStudentVerified = false;
        if (this.isVerifiedStudent == null)
            this.isVerifiedStudent = false;
        if (this.isActive == null)
            this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Users(UsersDTO usersDTO) {
        this.userId = usersDTO.getUserId();
        this.email = usersDTO.getEmail();
        this.password = usersDTO.getPassword();
        this.name = usersDTO.getName();
        this.nickname = usersDTO.getNickname();
        this.username = usersDTO.getUsername();
        this.role = Role.fromString(usersDTO.getRole());
        this.emailVerified = usersDTO.getEmailVerified() != null ? usersDTO.getEmailVerified() : false;
        this.isStudentVerified = usersDTO.getIsStudentVerified() != null ? usersDTO.getIsStudentVerified() : false;
        this.isActive = usersDTO.getIsActive() != null ? usersDTO.getIsActive() : true;
        this.oauthProvider = usersDTO.getOauthProvider();
        this.oauthId = usersDTO.getOauthId();
        this.profileImageUrl = usersDTO.getProfileImageUrl();
        this.gradeYear = usersDTO.getGradeYear();
        this.interests = usersDTO.getInterests();
        this.phone = usersDTO.getPhone();
    }
}
