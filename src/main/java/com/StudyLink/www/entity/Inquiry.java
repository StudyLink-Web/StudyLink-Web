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

    @Column(name = "user_content", nullable = false, columnDefinition = "TEXT")
    private String userContent;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "admin_content", columnDefinition = "TEXT")
    private String adminContent;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "is_public", length = 1, nullable = false)
    private String isPublic;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "writer_email", length = 100, nullable = false)
    private String writerEmail;

    @Column(name = "choose", length = 100)
    private String choose;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "answer_at")
    private LocalDateTime answerAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.isPublic == null || this.isPublic.isBlank()) this.isPublic = "Y";
        if (this.userContent == null) this.userContent = "";
        if (this.content == null) this.content = "";
        if (this.writerEmail == null) this.writerEmail = "";
    }
}
