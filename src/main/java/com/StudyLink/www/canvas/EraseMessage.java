package com.StudyLink.www.canvas;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class EraseMessage {
    private long senderId;
    private double x;
    private double y;
}
