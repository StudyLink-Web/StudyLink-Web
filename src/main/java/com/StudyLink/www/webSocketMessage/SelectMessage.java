package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class SelectMessage {
    private String senderId;
    private int seq;
    private Position[] positions;
}
