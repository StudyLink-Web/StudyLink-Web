package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "comment") // 테이블명이 reserved면 comments로 바꾸는 것도 추천
public class Comment extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cno;

    // ✅ bno -> postId (Board.post_id FK)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(length = 200, nullable = false)
    private String writer;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
