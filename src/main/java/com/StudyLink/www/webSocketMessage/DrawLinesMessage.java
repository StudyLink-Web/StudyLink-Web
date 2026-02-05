package com.StudyLink.www.webSocketMessage;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class DrawLinesMessage {
    private long senderId;
    private int seq;
    private List<LineData> messages; // 여기서 List 타입 지정

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class LineData {
        private String uuid;
        private double x1;
        private double y1;
        private double x2;
        private double y2;
        private String stroke;
    }
}
