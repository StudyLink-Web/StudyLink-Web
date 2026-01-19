package com.StudyLink.www.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDTO {

    // 게시글 PK
    private Long postId;

    // 작성자 FK (Users PK)
    private Long userId;

    // 로그인 아이디 (문자열)
    private String writer;

    // 학과
    private String department;

    // 게시글 제목
    private String title;

    // 게시글 본문
    private String content;

    // 조회수
    private int viewCount;

    // 작성 / 수정 시각 (TimeBase와 매칭)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String thumbPath;
}
