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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String email;
    private String name;
    private String nickname;

    @Column(length = 50)
    private String role; // 'STUDENT', 'MENTOR', 'ADMIN'

    @Column(length = 50)
    private String oauthProvider; // 'google', 'kakao', 'naver'

    private String oauthId;
    private String profileImageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
