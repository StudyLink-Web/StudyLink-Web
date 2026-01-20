package com.StudyLink.www.dto;

import com.StudyLink.www.entity.DrawData;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrawDataDTO {
    private String id; // MongoDB ObjectId
    private long roomId;
    private long senderId;
    private String uuid;
    private double x1;
    private double y1;
    private double x2;
    private double y2;

    public DrawDataDTO(DrawData drawData){
        this.id = drawData.getId();
        this.roomId = drawData.getRoomId();
        this.senderId = drawData.getSenderId();
        this.uuid = drawData.getUuid();
        this.x1 = drawData.getX1();
        this.y1 = drawData.getY1();
        this.x2 = drawData.getX2();
        this.y2 = drawData.getY2();
    }
}
