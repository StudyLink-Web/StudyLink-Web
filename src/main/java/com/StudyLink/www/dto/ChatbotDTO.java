package com.StudyLink.www.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChatbotDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String query;
        private List<Message> history;
        @com.fasterxml.jackson.annotation.JsonProperty("user_scores")
        @com.fasterxml.jackson.annotation.JsonAlias("userScores")
        private List<UserScore> userScores;
        @JsonAlias({"sessionId", "session_id"})
        private Long sessionId; // 추가: 대화 세션 ID (아카이브용)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserScore {
        @com.fasterxml.jackson.annotation.JsonProperty("subject_name")
        @com.fasterxml.jackson.annotation.JsonAlias("subjectName")
        private String subjectName;
        private Double score;
        @com.fasterxml.jackson.annotation.JsonProperty("score_type")
        @com.fasterxml.jackson.annotation.JsonAlias("scoreType")
        private String scoreType;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;    // "user" or "assistant"
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String answer;
        private String detected_univ;
        private List<String> found_majors;
        private String title; // 추가: AI가 생성한 대화 제목
    }
}
