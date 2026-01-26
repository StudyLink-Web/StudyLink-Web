package com.StudyLink.www.entity;

import com.StudyLink.www.dto.UsersDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Users (사용자 통합)
 * 학생과 멘토를 하나의 테이블로 관리
 * role로 구분: STUDENT, MENTOR, ADMIN
 */
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
    private String password; // BCrypt 암호화된 비밀번호

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    /**
     * 사용자 역할
     * STUDENT: 학생
     * MENTOR: 멘토
     * ADMIN: 관리자
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;  // 기본값 설정

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    // 학생 인증 여부 (학생 신분증 또는 재학증명서 확인)
    @Column(name = "is_student_verified", nullable = false)
    private Boolean isStudentVerified = false;

    // 학교 이메일 인증 관련 필드
    @Column(name = "school_email", unique = true, nullable = true)
    private String schoolEmail;  // 학교 이메일 (예: student@ewha.ac.kr)

    @Column(name = "is_verified_student", nullable = false)
    private Boolean isVerifiedStudent = false;  // 학교 이메일 인증 여부

    @Column(name = "school_email_verification_token", nullable = true)
    private String schoolEmailVerificationToken;  // 이메일 인증 토큰

    @Column(name = "school_email_token_expires", nullable = true)
    private LocalDateTime schoolEmailTokenExpires;  // 토큰 만료 시간

    @Column(name = "school_email_verified_at", nullable = true)
    private LocalDateTime schoolEmailVerifiedAt;  // 학교 이메일 인증 완료 시간

    // 마지막 이메일 전송 시간 (재전송 쿨다운용)
    @Column(name = "last_email_sent_at", nullable = true)
    private LocalDateTime lastEmailSentAt;  // 마지막 이메일 전송 시간

    /**
     * 가입일 (코호트 분석용)
     * 가입 시점을 기준으로 공통 특성 분석
     * 예: 3월 가입자는 내신 성적, 11월 가입자는 정시 성적 조회 패턴
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // OAuth2 관련 필드
    @Column(length = 50, nullable = true)
    private String oauthProvider;  // oauth_provider로 매핑

    @Column(length = 100, nullable = true)
    private String oauthId;  // oauth_id로 매핑

    @Column(length = 500, nullable = true)
    private String profileImageUrl;  // profile_image_url로 매핑

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    @Column(nullable = true)
    private String gradeYear;

    @Column(nullable = true)
    private String interests;

    @Column(nullable = true)
    private String phone;


    /**
     * 1:1 관계
     * 학생 상세 정보 (StudentProfile)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private StudentProfile studentProfile;

    /**
     * 1:1 관계
     * 멘토 상세 정보 (MentorProfile)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private MentorProfile mentorProfile;

    /**
     * 1:N 관계
     * 멘토 활동 가능 시간 (MentorAvailability)
     */
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MentorAvailability> mentorAvailabilities;

    /**
     * 1:N 관계
     * 즐겨찾기한 멘토 목록 (Favorite - 학생이 저장)
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favoritedMentors;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
        // 학생 인증 여부 기본값
        if (this.isStudentVerified == null) {
            this.isStudentVerified = false;
        }
        // 추가: 학교 이메일 인증 여부 기본값
        if (this.isVerifiedStudent == null) {
            this.isVerifiedStudent = false;
        }
        // OAuth 사용자는 isActive 기본값 true
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // 🔹 DTO -> 엔티티 생성자
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