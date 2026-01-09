package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatBotArchiveDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResponse {
        private Long sessionId;
        private String title;
        private String updatedAt; // String으로 변경하여 직렬화 문제 원천 차단
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private Long messageId;
        private String role;
        private String content;
        private String createdAt; // String으로 변경
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private Long sessionId;
        private String query;
    }
}
