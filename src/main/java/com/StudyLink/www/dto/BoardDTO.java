package com.StudyLink.www.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {

    // 게시글 PK
    private Long postId;

    // 작성자 FK (Users PK)
    private Long userId;

    // 작성자 아이디 (username)
    private String writer;

    // 작성자 프로필 이미지 URL
    private String writerProfileImageUrl;

    // 학과
    private String department;

    // 게시글 제목
    private String title;

    // 게시글 본문
    private String content;

    // 조회수
    private int viewCount;

    // 작성 / 수정 시각
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 썸네일 경로
    private String thumbPath;
}
