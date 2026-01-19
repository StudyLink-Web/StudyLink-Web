package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class InitailizeCurrentActionMessage {
    private long senderId;
    private int seq;
    private String type;
}
