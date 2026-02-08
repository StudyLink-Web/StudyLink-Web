package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LineData {
    private String uuid;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private String stroke;
    private double strokeWidth;
}
