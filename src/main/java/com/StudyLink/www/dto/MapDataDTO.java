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
        private List<ChatbotDTO.UserScore> userScores;
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
