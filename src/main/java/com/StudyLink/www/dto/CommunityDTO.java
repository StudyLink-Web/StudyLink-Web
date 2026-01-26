// src/main/java/com/StudyLink/www/dto/CommunityDTO.java
package com.StudyLink.www.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityDTO {

    private Long bno;
    private Long userId;

    private String email;
    private String role;

    private String title;
    private String writer;
    private String department;
    private String content;

    private Integer readCount;
    private Integer cmtQty;
    private Integer fileQty;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ board랑 동일하게 파일 리스트를 DTO에 들고있게
    private List<FileDTO> fileDTOList;
}
