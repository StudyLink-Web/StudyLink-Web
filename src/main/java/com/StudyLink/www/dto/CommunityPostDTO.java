package com.StudyLink.www.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostDTO {
    private Long id;
    private String title;
    private String content;

    private Long writerId;
    private String writerEmail; // ✅ 추가

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<MultipartFile> uploadFiles;
}
