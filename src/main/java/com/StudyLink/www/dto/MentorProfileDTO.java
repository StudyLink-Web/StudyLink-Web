package com.StudyLink.www.dto;

import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfileDTO {

    private Long userId;

    private UsersDTO usersDTO; // Users 엔티티를 DTO로 포함

    private Long univId;
    private Long deptId;

    private String studentCardImg;
    private Boolean isVerified;
    private String introduction;

    private Double averageRating;
    private Integer quizCount;
    private Long point;
    private Long exp;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 프로필 수정용
    private String firstName; // Users.name
    private String nickname; // Users.nickname
    private String phone; // Users.phone
    private String bio; // introduction과 같은 내용

    // 수업 정보
    private List<String> subjects; // JSON: ["math", "korean"]
    private List<String> grades; // JSON: ["high", "adult"]
    private Integer pricePerHour;
    private Double minLessonHours;
    private String lessonType;
    private String lessonLocation;
    private String availableTime;

    // 계정 설정
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    // 알림 설정
    private Boolean notificationLesson;
    private Boolean notificationMessage;
    private Boolean notificationReview;

    // 엔티티 → DTO 변환용
    private String university; // 대학교 이름
    private String major; // 학과 이름
    private Integer entranceYear;
    private Integer graduationYear;
    private String credentials;
    private String profileImageUrl; // 프로필 이미지 URL

    private Long lessonCount;
    private Long reviewCount;
    private String mentorNickname;

    private Boolean phoneVerified; // 이번 요청에서 전화번호 인증 여부

    // 🔹 엔티티 -> DTO 생성자
    public MentorProfileDTO(MentorProfile profile, UsersDTO usersDTO) {
        this.userId = profile.getUserId();
        this.usersDTO = usersDTO;
        this.univId = profile.getUnivId();
        this.deptId = profile.getDeptId();
        this.studentCardImg = profile.getStudentCardImg();
        // ✅ 멘토 인증 or 학생 인증 or 학교 이메일 인증 중 하나라도 되면 "검증된 멘토"
        this.isVerified = Boolean.TRUE.equals(profile.getIsVerified())
                || Boolean.TRUE.equals(profile.getUser().getIsStudentVerified())
                || Boolean.TRUE.equals(profile.getUser().getIsVerifiedStudent());
        this.introduction = profile.getIntroduction();
        this.averageRating = profile.getAverageRating();
        this.quizCount = profile.getQuizCount();
        this.point = profile.getPoint();
        this.exp = profile.getExp();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();

        // ✅ 프로필 수정용 필드들 초기화
        this.subjects = profile.getSubjects();
        this.grades = profile.getGrades();
        this.pricePerHour = profile.getPricePerHour();
        this.minLessonHours = profile.getMinLessonHours();
        this.lessonType = profile.getLessonType();
        this.lessonLocation = profile.getLessonLocation();
        this.availableTime = profile.getAvailableTime();
        this.notificationLesson = profile.getNotificationLesson();
        this.notificationMessage = profile.getNotificationMessage();
        this.notificationReview = profile.getNotificationReview();

        this.university = profile.getUniversity();
        this.major = profile.getMajor();
        this.entranceYear = profile.getEntranceYear();
        this.graduationYear = profile.getGraduationYear();
        this.credentials = profile.getCredentials();
        this.profileImageUrl = usersDTO.getProfileImageUrl(); // ✅ 프로필 이미지 연동 (수정됨)

    }
}
