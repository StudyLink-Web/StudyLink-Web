package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Position {
    private String uuid;
    private double top;
    private double left;
}