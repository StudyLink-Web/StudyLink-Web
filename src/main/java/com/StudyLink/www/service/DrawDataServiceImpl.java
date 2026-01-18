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
    public void saveDrawData(List<DrawDataDTO> lines) {
        List<DrawData> entities = lines.stream()
                .map(DrawData::new)
                .toList();
        drawDataRepository.saveAll(entities);
    }

    @Override
    public List<DrawDataDTO> findByRoomId(long roomId) {
        return drawDataRepository.findByRoomId(roomId)
                .stream()
                .map(DrawDataDTO::new)
                .toList();
    }
}
