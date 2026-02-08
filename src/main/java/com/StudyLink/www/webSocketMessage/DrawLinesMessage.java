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
}
