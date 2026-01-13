package com.StudyLink.www.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO {
    private Long cno;

    @JsonAlias({"bno"})  // ✅ bno로 와도 postId에 매핑 (보험)
    private Long postId;

    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
