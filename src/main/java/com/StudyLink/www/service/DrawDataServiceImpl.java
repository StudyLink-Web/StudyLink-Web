package com.StudyLink.www.service;

import com.StudyLink.www.dto.DrawDataDTO;
import com.StudyLink.www.entity.DrawData;
import com.StudyLink.www.repository.DrawDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawDataServiceImpl implements DrawDataService {
    private final DrawDataRepository drawDataRepository;

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
        log.info("✅ draw 저장: {}개", drawDataList.size());
    }

    @Override
    public void erase(List<DrawData> drawDataList) {
        if (drawDataList == null || drawDataList.isEmpty()) return;

        // erase 액션은 UUID 기준으로 삭제
        drawDataList.forEach(data -> {
            if (data.getUuid() != null) {
                drawDataRepository.deleteByRoomIdAndUuid(data.getRoomId(), data.getUuid());
                log.info("🗑 erase UUID 삭제: {}", data.getUuid());
            }
        });
    }
}
