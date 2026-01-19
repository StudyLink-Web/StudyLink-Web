package com.StudyLink.www.webSocketMessage;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class UndoRedoMessage {
    private long senderId;
    private int seq;
    private String type;
}
