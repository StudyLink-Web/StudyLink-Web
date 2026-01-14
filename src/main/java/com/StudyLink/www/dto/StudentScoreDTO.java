package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentScoreDTO {
    private Long scoreId;
    private String subjectName;
    private Double score;
    private String scoreType; // "표점", "등급"
    private String category;  // "공통", "사탐", "과탐"
}
