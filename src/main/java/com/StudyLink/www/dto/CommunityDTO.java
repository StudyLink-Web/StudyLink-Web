package com.StudyLink.www.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityDTO {

    private Long bno;

    // ✅ CommunityController에서 사용 중
    private Long userId;
    private String email;
    private String role;

    private String title;
    private String writer;

    private int readCount;
    private int cmtQty;
    private int fileQty;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
