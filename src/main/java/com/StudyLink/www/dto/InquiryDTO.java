package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Inquiry;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDTO {

    private Long qno;

    private String title;
    private String userContent;
    private String adminContent;

    private String choose;
    private String category;
    private String status;

    private String isPublic;
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime answerAt;
}
