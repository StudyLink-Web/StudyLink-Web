package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomService {

    List<SubjectDTO> getSubjectDTOList();

    RoomDTO createRoom(long studentId);

    Page<RoomDTO> getRoomList(Pageable pageable);

    void update(long roomId, int subjectId, Long mentorId, int point);

    Page<RoomDTO> getPrivateRoomList(long mentorId, Pageable pageable);
}
