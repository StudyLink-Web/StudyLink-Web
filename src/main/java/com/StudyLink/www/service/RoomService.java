package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;

import java.util.List;

public interface RoomService {

    List<SubjectDTO> getSubjectDTOList();

    RoomDTO createRoom(long studentId);

    List<RoomDTO> getRoomList();

    void update(long roomId, int subjectId, Long mentorId, int point);

    List<RoomDTO> getPrivateRoomList(long mentorId);
}
