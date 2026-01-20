package com.StudyLink.www.service;

import com.StudyLink.www.dto.DrawDataDTO;
import com.StudyLink.www.dto.UndoRedoStackDTO;
import com.StudyLink.www.entity.DrawData;
import com.StudyLink.www.entity.UndoRedoStack;

import java.util.List;

public interface DrawDataService {

    List<DrawDataDTO> findByRoomId(long roomId);

    void draw(List<DrawData> drawDataList);

    void erase(List<DrawData> drawDataList);

    void removeRoom(long roomId);

    void pushUndoRedoStack(UndoRedoStackDTO undoRedoStackDTO);

    UndoRedoStack getUndoRedoStack(long roomId);
}
