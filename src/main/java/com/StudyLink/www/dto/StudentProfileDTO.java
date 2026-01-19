package com.StudyLink.www.dto;

import com.StudyLink.www.entity.StudentProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDTO {

    private Long userId;

    private UsersDTO usersDTO;

    private String targetUniversity;
    private String targetMajor;
    private String regionPreference;

    private int chargedPoint;   // 직접 충전한 포인트
    private int bonusPoint;     // 매주 지급되는 포인트

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔹 엔티티 -> DTO 생성자
    public StudentProfileDTO(StudentProfile profile) {
        this.userId = profile.getUserId();

        if (profile.getUser() != null) {
            this.usersDTO = new UsersDTO(profile.getUser());
        }

        this.targetUniversity = profile.getTargetUniversity();
        this.targetMajor = profile.getTargetMajor();
        this.regionPreference = profile.getRegionPreference();
        this.chargedPoint = profile.getChargedPoint();
        this.bonusPoint = profile.getBonusPoint();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();
    }
}