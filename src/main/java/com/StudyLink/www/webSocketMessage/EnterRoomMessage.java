package com.StudyLink.www.webSocketMessage;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EnterRoomMessage {
    private long roomId;
}
