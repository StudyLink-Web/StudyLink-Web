package com.StudyLink.www.canvas;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class SenderMessage {
    private long senderId;
    private int seq;
}
