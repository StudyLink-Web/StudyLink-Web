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

    /**
     * 대학교 이름
     * 예: "서울대학교"
     */
    @Column(name = "university", length = 255)
    private String university;

    /**
     * 학과 이름
     * 예: "수학교육과"
     */
    @Column(name = "major", length = 255)
    private String major;

    /**
     * 입학년도
     */
    @Column(name = "entrance_year")
    private Integer entranceYear;

    /**
     * 졸업년도
     */
    @Column(name = "graduation_year")
    private Integer graduationYear;

    /**
     * 추가 자격사항
     * 예: "교사 자격증, 공인 자격증 등"
     */
    @Column(name = "credentials", columnDefinition = "TEXT")
    private String credentials;

    /**
     * 주요 과목 (JSON 형식)
     * 예: ["math", "korean", "english"]
     */
    @Column(name = "subjects", columnDefinition = "JSON")
    private String subjects;

    /**
     * 수업 대상 학년 (JSON 형식)
     * 예: ["middle", "high", "adult"]
     */
    @Column(name = "grades", columnDefinition = "JSON")
    private String grades;

    /**
     * 시간당 수업료 (원)
     */
    @Column(name = "price_per_hour")
    private Integer pricePerHour;

    /**
     * 최소 수업 시간
     * 예: 1, 1.5, 2
     */
    @Column(name = "min_lesson_hours")
    private Double minLessonHours;

    /**
     * 수업 방식
     * "online", "offline", "both"
     */
    @Column(name = "lesson_type", length = 50)
    private String lessonType;

    /**
     * 오프라인 수업 위치
     * 예: "서울시 강남구"
     */
    @Column(name = "lesson_location", length = 255)
    private String lessonLocation;

    /**
     * 수업 가능 시간
     * 예: "평일 오후 6시 이후, 주말 자유"
     */
    @Column(name = "available_time", columnDefinition = "TEXT")
    private String availableTime;

    /**
     * 새 수업 요청 알림
     */
    @Column(name = "notification_lesson", nullable = false)
    private Boolean notificationLesson = true;

    /**
     * 메시지 알림
     */
    @Column(name = "notification_message", nullable = false)
    private Boolean notificationMessage = true;

    /**
     * 리뷰 작성 알림
     */
    @Column(name = "notification_review", nullable = false)
    private Boolean notificationReview = true;

    /**
     * 수업 횟수 (총 진행한 수업 개수)
     */
    @Column(name = "lesson_count", nullable = false)
    private Long lessonCount = 0L;

    /**
     * 리뷰 개수 (받은 리뷰의 개수)
     */
    @Column(name = "review_count", nullable = false)
    private Long reviewCount = 0L;

    /**
     * 멘토 닉네임 (학생들이 보는 이름)
     * Users.nickname과 동기화
     */
    @Column(name = "mentor_nickname", length = 100)
    private String mentorNickname;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.point == null) this.point = 0L;
        if (this.exp == null) this.exp = 0L;
        if (this.averageRating == null) this.averageRating = 0.0;
        if (this.isVerified == null) this.isVerified = false;
        if (this.notificationLesson == null) this.notificationLesson = true;
        if (this.notificationMessage == null) this.notificationMessage = true;
        if (this.notificationReview == null) this.notificationReview = true;
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

        // 추가된 필드들 초기화
        this.university = dto.getUniversity();
        this.major = dto.getMajor();
        this.entranceYear = dto.getEntranceYear();
        this.graduationYear = dto.getGraduationYear();
        this.credentials = dto.getCredentials();
        this.subjects = dto.getSubjects();
        this.grades = dto.getGrades();
        this.pricePerHour = dto.getPricePerHour();
        this.minLessonHours = dto.getMinLessonHours();
        this.lessonType = dto.getLessonType();
        this.lessonLocation = dto.getLessonLocation();
        this.availableTime = dto.getAvailableTime();
        this.notificationLesson = dto.getNotificationLesson() != null ? dto.getNotificationLesson() : true;
        this.notificationMessage = dto.getNotificationMessage() != null ? dto.getNotificationMessage() : true;
        this.notificationReview = dto.getNotificationReview() != null ? dto.getNotificationReview() : true;
    }


}
