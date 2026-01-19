package com.StudyLink.www.service;

import com.StudyLink.www.dto.DrawDataDTO;
import com.StudyLink.www.entity.DrawData;

import java.util.List;

public interface DrawDataService {

    List<DrawDataDTO> findByRoomId(long roomId);

    void draw(List<DrawData> drawDataList);

    void erase(List<DrawData> drawDataList);
}
