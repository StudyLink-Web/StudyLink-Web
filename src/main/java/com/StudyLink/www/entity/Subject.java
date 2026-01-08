package com.StudyLink.www.entity;

import com.StudyLink.www.dto.SubjectDTO;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "subject")
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subjectId;

    @Column
    private String name;

    public Subject(SubjectDTO dto) {
        this.subjectId = dto.getSubjectId();
        this.name = dto.getName();
    }
}
