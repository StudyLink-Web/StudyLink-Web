package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class MapDataDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @com.fasterxml.jackson.annotation.JsonProperty("user_scores")
        @com.fasterxml.jackson.annotation.JsonAlias("userScores")
        private List<ChatbotDTO.UserScore> userScores;
        
        private Bounds bounds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bounds {
        @com.fasterxml.jackson.annotation.JsonProperty("minLat")
        @com.fasterxml.jackson.annotation.JsonAlias("min_lat")
        private Double minLat;
        @com.fasterxml.jackson.annotation.JsonProperty("maxLat")
        @com.fasterxml.jackson.annotation.JsonAlias("max_lat")
        private Double maxLat;
        @com.fasterxml.jackson.annotation.JsonProperty("minLng")
        @com.fasterxml.jackson.annotation.JsonAlias("min_lng")
        private Double minLng;
        @com.fasterxml.jackson.annotation.JsonProperty("maxLng")
        @com.fasterxml.jackson.annotation.JsonAlias("max_lng")
        private Double maxLng;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private List<Item> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String univ;
        private String major;
        private Double lat;
        private Double lng;
        private Double target_per;
        private String status;
        private String region;
        private String category;
    }
}
