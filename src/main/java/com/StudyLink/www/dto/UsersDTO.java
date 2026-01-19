package com.StudyLink.www.dto;

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
public class UsersDTO {
    private Long userId;

    private String email;
    private String password;
    private String name;
    private String nickname;
    private String username;
    private String role;

    private Boolean emailVerified;
    private Boolean isStudentVerified;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String oauthProvider;
    private String oauthId;
    private String profileImageUrl;

    private String gradeYear;
    private String interests;
    private String phone;

    // 🔹 엔티티 -> DTO 생성자
    public UsersDTO(Users user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.emailVerified = user.getEmailVerified();
        this.isStudentVerified = user.getIsStudentVerified();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.oauthProvider = user.getOauthProvider();
        this.oauthId = user.getOauthId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.gradeYear = user.getGradeYear();
        this.interests = user.getInterests();
        this.phone = user.getPhone();
    }
}
