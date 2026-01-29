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
    @Column(name = "qno")
    private Long qno;

    @Column(length = 100, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "answer_at")
    private LocalDateTime answerAt;

    @Column(length = 200, nullable = false)
    private String title;

    @Lob
    @Column(name = "user_content", nullable = false)
    private String userContent;

    @Lob
    @Column(name = "admin_content")
    private String adminContent;

    @Column(name = "is_public", length = 1, nullable = false)
    private String isPublic;   // Y / N

    @Column(length = 100)
    private String choose;

    /* 🔐 비공개 문의 비밀번호 (암호화해서 저장 권장) */
    @Column(length = 255)
    private String password;

    /* ===== 기본값 세팅 ===== */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "대기";
        }
        if (this.isPublic == null) {
            this.isPublic = "N";
        }
    }
}
