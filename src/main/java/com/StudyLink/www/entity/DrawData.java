package com.StudyLink.www.entity;

import com.StudyLink.www.dto.DrawDataDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "draw_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrawData {
    @Id
    private String id; // MongoDB ObjectId
    private long roomId;
    private long senderId;
    private String uuid;
    private String stroke;
    private double strokeWidth;
    private double x1;
    private double y1;
    private double x2;
    private double y2;

    public DrawData(DrawDataDTO drawDataDTO){
        this.id = drawDataDTO.getId();
        this.roomId = drawDataDTO.getRoomId();
        this.senderId = drawDataDTO.getSenderId();
        this.uuid = drawDataDTO.getUuid();
        this.stroke = drawDataDTO.getStroke();
        this.strokeWidth = drawDataDTO.getStrokeWidth();
        this.x1 = drawDataDTO.getX1();
        this.y1 = drawDataDTO.getY1();
        this.x2 = drawDataDTO.getX2();
        this.y2 = drawDataDTO.getY2();
    }
}
