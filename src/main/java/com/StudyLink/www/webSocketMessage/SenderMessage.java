package com.StudyLink.www.webSocketMessage;

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
