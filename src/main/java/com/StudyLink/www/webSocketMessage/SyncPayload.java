package com.StudyLink.www.webSocketMessage;

import com.StudyLink.www.dto.DrawDataDTO;
import com.StudyLink.www.entity.UndoRedoStack;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SyncPayload {
    private List<DrawDataDTO> drawData;
    private UndoRedoStack undoRedoStack;
}