package com.StudyLink.www.service;

import com.StudyLink.www.dto.DrawDataDTO;

import java.util.List;

public interface DrawDataService {

    void saveDrawData(List<DrawDataDTO> lines);

    List<DrawDataDTO> findByRoomId(long roomId);
}
