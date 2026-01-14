package com.StudyLink.www.dto;

import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfileDTO {

    private Long userId;

    private UsersDTO usersDTO;       // Users 엔티티를 DTO로 포함

    private Long univId;
    private Long deptId;

    private String studentCardImg;
    private Boolean isVerified;
    private String introduction;

    private Double averageRating;
    private Long point;
    private Long exp;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔹 엔티티 -> DTO 생성자
    public MentorProfileDTO(MentorProfile profile, UsersDTO usersDTO) {
        this.userId = profile.getUserId();
        this.usersDTO = usersDTO;
        this.univId = profile.getUnivId();
        this.deptId = profile.getDeptId();
        this.studentCardImg = profile.getStudentCardImg();
        this.isVerified = profile.getIsVerified();
        this.introduction = profile.getIntroduction();
        this.averageRating = profile.getAverageRating();
        this.point = profile.getPoint();
        this.exp = profile.getExp();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();
    }
}