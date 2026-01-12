package com.StudyLink.www.canvas;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class UndoRedoMessage {
    private long senderId;
    private String type;
}
