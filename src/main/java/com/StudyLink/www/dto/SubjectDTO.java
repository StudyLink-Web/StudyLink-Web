package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectDTO {
    private Integer subjectId;
    private String name;
    private String color;

    public SubjectDTO (Subject subject) {
        this.subjectId = subject.getSubjectId();
        this.name = subject.getName();
        this.color = subject.getColor();
    }
}
