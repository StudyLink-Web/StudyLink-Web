package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;

import java.util.List;

public interface RoomService {

    List<SubjectDTO> getSubjectDTOList();

    RoomDTO createRoom(long studentId);
}
