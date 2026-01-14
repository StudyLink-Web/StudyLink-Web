package com.StudyLink.www.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDTO {

    private String uuid;
    private String saveDir;
    private String fileName;
    private int fileType;
    private long fileSize;

    // 게시글 FK
    private Long postId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 썸네일 여부 판별 헬퍼
    public boolean isThumbnail() {
        return (uuid != null && uuid.startsWith("_th_"))
                || (fileName != null && fileName.contains("_th_"));
    }
}
