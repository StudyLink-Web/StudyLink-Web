package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class CircleMessage {
    private long senderId;
    private int seq;
    private String uuid;
    private String stroke;
    private double strokeWidth;
    private double centerX;
    private double centerY;
    private double x;
    private double y;
}
