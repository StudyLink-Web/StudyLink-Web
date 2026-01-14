package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.entity.Room;
import com.StudyLink.www.repository.MessageRepository;
import com.StudyLink.www.repository.RoomRepository;
import com.StudyLink.www.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
