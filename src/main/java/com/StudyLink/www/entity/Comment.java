package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "comments")
public class Comment extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cno;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(length = 200, nullable = false)
    private String writer;

    @Lob
    @Column(nullable = false)
    private String content;
}
