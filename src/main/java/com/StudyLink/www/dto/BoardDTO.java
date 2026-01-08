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
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private int viewCount;
    private LocalDateTime createdAt, updatedAt;
}
