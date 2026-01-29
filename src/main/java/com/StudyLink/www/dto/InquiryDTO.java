package com.StudyLink.www.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InquiryDTO {

    private Long qno;

    private String title;
    private String userContent;
    private String adminContent;

    private String category;   // ✅ builder에 포함
    private String status;

    private String isPublic;
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime answerAt;
}
