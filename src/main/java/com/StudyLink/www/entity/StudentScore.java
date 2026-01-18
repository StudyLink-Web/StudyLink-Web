package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long scoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName; // 예: 국어, 수학, 영어, 생활과 윤리 등

    @Column(nullable = false)
    private Double score; // 표준점수, 등급, 백분위 등 모든 타입 수용 (Double로 확장)

    @Column(name = "score_type", length = 20)
    private String scoreType; // "표점", "등급"

    @Column(length = 20)
    private String category; // "공통", "사탐", "과탐"

    @Column(name = "optional_subject", length = 50)
    private String optionalSubject; // [v4] 국어(화작/언매), 수학(확통/미적/기하)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private ScoreRecord scoreRecord;
}
