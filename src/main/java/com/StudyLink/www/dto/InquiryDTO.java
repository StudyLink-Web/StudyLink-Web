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
    private String writerEmail;

    private LocalDateTime createdAt;
    private LocalDateTime answerAt;

    public InquiryDTO(Inquiry inquiry) {
        this.qno = inquiry.getQno();
        this.title = inquiry.getTitle();
        this.userContent = inquiry.getUserContent();
        this.adminContent = inquiry.getAdminContent();

        this.choose = inquiry.getChoose();        // ✅ fix
        this.category = inquiry.getCategory();    // ✅ fix
        this.status = inquiry.getStatus();

        this.isPublic = inquiry.getIsPublic();
        this.password = inquiry.getPassword();
        this.writerEmail = inquiry.getWriterEmail(); // ✅ fix

        this.createdAt = inquiry.getCreatedAt();
        this.answerAt = inquiry.getAnswerAt();
    }
}
