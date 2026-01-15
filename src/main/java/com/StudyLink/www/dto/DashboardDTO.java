package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class DashboardDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("user_id")
        private Long userId;
        private String name;
        @com.fasterxml.jackson.annotation.JsonProperty("user_scores")
        private List<StudentScoreDTO> userScores;
        @com.fasterxml.jackson.annotation.JsonProperty("target_university")
        private String targetUniversity;
        @com.fasterxml.jackson.annotation.JsonProperty("target_major")
        private String targetMajor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("chart_data")
        private Map<String, Object> chartData;
        @com.fasterxml.jackson.annotation.JsonProperty("radar_data")
        private Map<String, Object> radarData;
        @com.fasterxml.jackson.annotation.JsonProperty("ai_summary")
        private String aiSummary;
        @com.fasterxml.jackson.annotation.JsonProperty("gap_analysis")
        private Map<String, Double> gapAnalysis;
        @com.fasterxml.jackson.annotation.JsonProperty("top_universities")
        private Map<String, Object> topUniversities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusResponse {
        private boolean hasScores;
        private String lastUpdatedAt;
    }
}
