package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "community_comment")
public class CommunityComment extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cno")
    private Long cno;

    @Column(name = "bno", nullable = false)
    private Long bno;

    @Column(name = "writer", length = 100, nullable = false)
    private String writer;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;
}
