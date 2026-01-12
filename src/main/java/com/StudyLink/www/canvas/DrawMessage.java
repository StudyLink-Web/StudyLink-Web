package com.StudyLink.www.canvas;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class DrawMessage {
    private long senderId;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
}
