package com.StudyLink.www.canvas;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class InitailizeCurrentActionMessage {
    private long senderId;
    private String type;
}
