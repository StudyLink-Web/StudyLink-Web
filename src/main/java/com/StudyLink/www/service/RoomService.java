package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomService {

    List<SubjectDTO> getSubjectDTOList();

    RoomDTO createRoom(long studentId);

    Page<RoomDTO> getRoomList(Pageable pageable);

    void save(RoomDTO roomDTO);

    Page<RoomDTO> getPrivateRoomList(long mentorId, Pageable pageable);

    RoomDTO getRoomDTO(long roomId);

    int updateStatusIfPending(long roomId, Room.Status newStatus);

    int deleteIfPending(long roomId);

    void deleteRoom(long roomId);

    void deleteMentorMessage(long roomId, long mentorId);

    Page<RoomDTO> getMyQuizList(long userId, Pageable pageable);
}
