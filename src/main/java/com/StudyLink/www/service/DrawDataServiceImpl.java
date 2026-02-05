package com.StudyLink.www.service;

import com.StudyLink.www.dto.DrawDataDTO;
import com.StudyLink.www.dto.UndoRedoStackDTO;
import com.StudyLink.www.entity.DrawData;
import com.StudyLink.www.entity.UndoRedoStack;
import com.StudyLink.www.repository.DrawDataRepository;
import com.StudyLink.www.repository.UndoRedoStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawDataServiceImpl implements DrawDataService {
    private final DrawDataRepository drawDataRepository;
    private final UndoRedoStackRepository undoRedoStackRepository;

    @Override
    public List<DrawDataDTO> findByRoomId(long roomId) {
        return drawDataRepository.findByRoomId(roomId)
                .stream()
                .map(DrawDataDTO::new)
                .toList();
    }

    @Override
    public void draw(List<DrawData> drawDataList) {
        if (drawDataList == null || drawDataList.isEmpty()) return;

        // draw 액션은 새로운 선들을 DB에 저장
        drawDataRepository.saveAll(drawDataList);
    }

    @Override
    public void erase(List<DrawData> drawDataList) {
        if (drawDataList == null || drawDataList.isEmpty()) return;

        // erase 액션은 UUID 기준으로 삭제
        drawDataList.forEach(data -> {
            if (data.getUuid() != null) {
                drawDataRepository.deleteByRoomIdAndUuid(data.getRoomId(), data.getUuid());
            }
        });
    }

    @Override
    public void removeRoom(long roomId) {
        drawDataRepository.deleteByRoomId(roomId);
        undoRedoStackRepository.deleteByRoomId(roomId);
    }

    @Override
    public void pushUndoRedoStack(UndoRedoStackDTO undoRedoStackDTO) {
        UndoRedoStack stack = undoRedoStackRepository.findByRoomId(undoRedoStackDTO.getRoomId())
                .orElse(UndoRedoStack.builder()
                        .roomId(undoRedoStackDTO.getRoomId())
                        .undoStack(new ArrayList<>())
                        .redoStack(new ArrayList<>())
                        .build());

        // undoStack, redoStack 덮어쓰기
        stack.setUndoStack(new UndoRedoStack(undoRedoStackDTO).getUndoStack());
        stack.setRedoStack(new UndoRedoStack(undoRedoStackDTO).getRedoStack());
        // 저장
        undoRedoStackRepository.save(stack);
    }

    @Override
    public UndoRedoStack getUndoRedoStack(long roomId) {
        return undoRedoStackRepository.findByRoomId(roomId)
                .orElse(UndoRedoStack.builder()
                        .roomId(roomId)
                        .undoStack(new ArrayList<>())
                        .redoStack(new ArrayList<>())
                        .build());
    }
}
