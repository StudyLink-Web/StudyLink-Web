package com.StudyLink.www.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentScoreDTO {
    @JsonProperty("score_id")
    private Long scoreId;
    
    @JsonProperty("subject_name")
    private String subjectName;
    
    @JsonProperty("score")
    private Double score;
    
    @JsonProperty("score_type")
    private String scoreType; // "표점", "등급"
    
    @JsonProperty("category")
    private String category;  // "공통", "사탐", "과탐"

    @JsonProperty("optional_subject")
    private String optionalSubject; // [v4] 국어(화작/언매), 수학(확통/미적/기하)
}
