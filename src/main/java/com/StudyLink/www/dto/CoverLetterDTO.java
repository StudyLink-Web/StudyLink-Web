package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class CoverLetterDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String title;
        private Integer questionNum;
        private String questionText;
        private List<String> keywords; // 학생이 입력한 경험 키워드
        private String tone; // 문체 (예: 논리적인, 열정적인)
        private String targetUniversity;
        private String targetMajor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long coverLetterId;
        private String title;
        private Integer questionNum;
        private String content;
        private String targetUniversity;
        private String targetMajor;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIRequest {
        private String name;
        private String university;
        private String major;
        private String question;
        private List<String> keywords;
        private String tone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIResponse {
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractRequest {
        private String rawText; // 사용자가 붙여넣은 생기부 텍스트
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractResponse {
        private List<String> keywords; // 추출된 키워드
        private String suggestedTitle; // 추천 자소서 제목
        private String summary; // 활동 요약
    }
}
