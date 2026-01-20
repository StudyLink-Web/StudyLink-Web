package com.StudyLink.www.dto;

import com.StudyLink.www.entity.UndoRedoStack;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UndoRedoStackDTO {

    private long roomId;        // 방 ID

    private List<CanvasActionDTO> undoStack;
    private List<CanvasActionDTO> redoStack;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class CanvasActionDTO {
        private String type; // 'draw', 'erase', 'select', 'move', 'rotate', 'scale' 등
        private List<Map<String, Object>> targets; // 영향을 받은 객체들
        private List<Map<String,Object>> before;  // 작업 전 상태
        private List<Map<String,Object>> after;   // 작업 후 상태
    }

    // 엔티티 -> DTO 변환 생성자
    public UndoRedoStackDTO(UndoRedoStack undoRedoStack) {
        this.roomId = undoRedoStack.getRoomId();
        if (undoRedoStack.getUndoStack() != null) {
            this.undoStack = undoRedoStack.getUndoStack().stream()
                    .map(a -> new CanvasActionDTO(a.getType(), a.getTargets(), a.getBefore(), a.getAfter()))
                    .toList();
        }
        if (undoRedoStack.getRedoStack() != null) {
            this.redoStack = undoRedoStack.getRedoStack().stream()
                    .map(a -> new CanvasActionDTO(a.getType(), a.getTargets(), a.getBefore(), a.getAfter()))
                    .toList();
        }
    }
}