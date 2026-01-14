package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.entity.Room;
import com.StudyLink.www.entity.Subject;
import com.StudyLink.www.repository.MessageRepository;
import com.StudyLink.www.repository.RoomRepository;
import com.StudyLink.www.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService{
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public List<SubjectDTO> getSubjectDTOList() {
        return subjectRepository.findAll().stream().map(SubjectDTO::new).toList();
    }

    @Override
    public RoomDTO createRoom(long studentId) {
        Room room = Room.builder()
                .studentId(studentId)
                .build();
        return new RoomDTO(roomRepository.save(room));
    }

    @Override
    public List<RoomDTO> getRoomList() {
        List<Room> roomList = roomRepository.findByStatusAndIsPublic(Room.Status.PENDING, true);
        return roomList.stream().map(RoomDTO::new).toList();
    }

    @Transactional
    @Override
    public void update(long roomId, int subjectId, Long mentorId, int point) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 방이 없습니다."));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("해당 방이 없습니다."));

        room.setSubject(subject);
        if (mentorId != null) {
            room.setMentorId(mentorId);
            room.setIsPublic(false);
        }
        room.setPoint(point);
        room.setStatus(Room.Status.PENDING);
    }

    @Override
    public List<RoomDTO> getPrivateRoomList(long mentorId) {
        List<Room> roomList = roomRepository.findByStatusAndIsPublicAndMentorId(Room.Status.PENDING, false, mentorId);
        return roomList.stream().map(RoomDTO::new).toList();
    }
}
