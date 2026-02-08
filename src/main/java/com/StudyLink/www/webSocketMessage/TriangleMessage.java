package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class TriangleMessage {
    private long senderId;
    private int seq;
    private String uuid;
    private String stroke;
    private double strokeWidth;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private double x3;
    private double y3;
}
