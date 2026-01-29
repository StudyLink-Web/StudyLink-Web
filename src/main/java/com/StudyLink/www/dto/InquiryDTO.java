package com.StudyLink.www.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDTO {

    private Long qno;                 // 문의 번호

    private String status;            // 상태 (대기, 처리중, 완료)

    private LocalDateTime createdAt;  // 문의 등록일
    private LocalDateTime answerAt;   // 답변 일시

    private String title;             // 문의 제목

    private String userContent;       // 사용자 문의 내용
    private String adminContent;      // 관리자 답변 내용

    private String isPublic;          // 공개 여부 (Y / N)
    private String choose;            // 문의 유형 (결제, 계정, 기타 등)

    // ✅ 비공개 문의 비밀번호 (비공개일 때만 사용)
    private String password;

    /* ===== 화면용 필드 (선택) ===== */
    private String writerEmail;       // 작성자 이메일
}
