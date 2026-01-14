package com.StudyLink.www.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentDTO {

    private Long cno;

    @JsonProperty("postId")
    @JsonAlias({"bno"})
    private Long postId;

    private String writer;
    private String content;

    @JsonAlias({"regDate", "createdAt"})
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
