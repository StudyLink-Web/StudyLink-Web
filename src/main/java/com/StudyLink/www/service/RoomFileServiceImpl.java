package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomFileDTO;
import com.StudyLink.www.entity.RoomFile;
import com.StudyLink.www.repository.RoomFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomFileServiceImpl implements RoomFileService{
    private final RoomFileRepository roomFileRepository;

    @Override
    public void insert(RoomFileDTO roomFileDTO) {
        roomFileRepository.save(new RoomFile(roomFileDTO));
    }

    @Override
    public RoomFileDTO loadFile(String uuid) {
        return new RoomFileDTO(roomFileRepository
                .findById(uuid).orElseThrow(() -> new EntityNotFoundException("해당 파일이 없습니다.")));
    }

}
