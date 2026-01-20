package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * CoverLetter (대입 자기소개서)
 * 사용자가 생성하거나 편집한 자소서 내용을 저장
 */
@Entity
@Table(name = "cover_letters")
@Data
@EqualsAndHashCode(callSuper = false) // Lint fix: explicitly handle superclass fields
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverLetter extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_letter_id")
    private Long coverLetterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(length = 200, nullable = false)
    private String title; // 자소서 제목 (예: 서울대 화공과 1차)

    @Column(name = "question_num")
    private Integer questionNum; // 문항 번호 (1, 2, 3 등)

    @Column(columnDefinition = "TEXT")
    private String questionText; // 문항 내용

    @Column(columnDefinition = "LONGTEXT")
    private String content; // 자소서 본문 내용

    @Column(length = 100)
    private String targetUniversity; // 지원 대학교

    @Column(length = 100)
    private String targetMajor; // 지원 학과

    @Builder.Default
    @Column(length = 50)
    private String status = "DRAFT"; // 상태 (DRAFT, FINAL)

}
