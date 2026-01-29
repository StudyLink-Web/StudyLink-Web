package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qno;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userContent;   // 사용자 문의 내용

    @Column(columnDefinition = "TEXT")
    private String adminContent;  // 관리자 답변

    @Column(length = 20)
    private String category;      // CAT1, CAT2

    @Column(length = 20)
    private String status;        // PENDING, READY, COMPLETE

    @Column(length = 1)
    private String isPublic;      // Y / N

    @Column(length = 255)
    private String password;

    private LocalDateTime createdAt;

    private LocalDateTime answerAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
