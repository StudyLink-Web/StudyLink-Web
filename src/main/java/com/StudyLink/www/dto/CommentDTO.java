package com.StudyLink.www.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@ToString
@AllArgsConstructor @NoArgsConstructor
@Builder
public class CommentDTO {
    private Long cno;
    private Long postId;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
