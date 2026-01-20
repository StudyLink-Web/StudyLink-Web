package com.StudyLink.www.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanvasActionRequestDTO {
    private long roomId;
    private long senderId;
    private String actionType; // draw | erase | select
    private List<Map<String, Object>> payload; // draw: line 배열, erase: uuid 배열, select: 위치 정보 배열
}