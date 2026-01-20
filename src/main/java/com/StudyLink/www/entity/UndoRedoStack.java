package com.StudyLink.www.entity;

import com.StudyLink.www.dto.UndoRedoStackDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "undo_redo_stack")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UndoRedoStack {
    @Id
    private String id;          // MongoDB ObjectId

    private long roomId;        // 방 ID

    // undo / redo 스택: currentAction 구조 그대로 저장
    private List<CanvasAction> undoStack;
    private List<CanvasAction> redoStack;

    // 내부 static 클래스: currentAction 구조
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class CanvasAction {
        private String type; // 'draw', 'erase', 'select', 'move', 'rotate', 'scale' 등
        private List<Map<String, Object>> targets; // 영향을 받은 객체들 (line, uuid, position 등)
        private List<Map<String,Object>> before;       // 작업 전 상태 (select/transform용)
        private List<Map<String,Object>> after;        // 작업 후 상태 (select/transform용)
    }

    // DTO -> 엔티티 변환 생성자
    public UndoRedoStack(UndoRedoStackDTO undoRedoStackDTO) {
        this.roomId = undoRedoStackDTO.getRoomId();
        if (undoRedoStackDTO.getUndoStack() != null) {
            this.undoStack = undoRedoStackDTO.getUndoStack().stream()
                    .map(a -> new UndoRedoStack.CanvasAction(a.getType(), a.getTargets(), a.getBefore(), a.getAfter()))
                    .toList();
        }
        if (undoRedoStackDTO.getRedoStack() != null) {
            this.redoStack = undoRedoStackDTO.getRedoStack().stream()
                    .map(a -> new UndoRedoStack.CanvasAction(a.getType(), a.getTargets(), a.getBefore(), a.getAfter()))
                    .toList();
        }
    }
}