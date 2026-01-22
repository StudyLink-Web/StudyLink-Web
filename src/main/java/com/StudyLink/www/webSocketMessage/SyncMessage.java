package com.StudyLink.www.webSocketMessage;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SyncMessage {
    private long roomId;
    private String type; // START | DATA | END
    private Object payload;
}
