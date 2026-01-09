package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomFileDTO;

public interface RoomFileService {
    void insert(RoomFileDTO roomFileDTO);

    RoomFileDTO loadFile(String uuid);
}
