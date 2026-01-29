package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.RoomFileDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.entity.Room;
import com.StudyLink.www.entity.Subject;
import com.StudyLink.www.handler.RoomFileHandler;
import com.StudyLink.www.repository.MessageRepository;
import com.StudyLink.www.repository.RoomFileRepository;
import com.StudyLink.www.repository.RoomRepository;
import com.StudyLink.www.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService{
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final SubjectRepository subjectRepository;
    private final RoomFileRepository roomFileRepository;
    private final RoomFileHandler roomFileHandler;

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
    public Page<RoomDTO> getRoomList(Pageable pageable) {
        return roomRepository.findByStatusAndIsPublic(Room.Status.PENDING, true, pageable).map(RoomDTO::new);
    }

    @Override
    public void save(RoomDTO roomDTO) {
        roomRepository.save(new Room(roomDTO));
    }

    @Override
    public Page<RoomDTO> getPrivateRoomList(long mentorId, Pageable pageable) {
        return roomRepository.findByStatusAndIsPublicAndMentorId(Room.Status.PENDING, false, mentorId, pageable).map(RoomDTO::new);
    }

    @Override
    public RoomDTO getRoomDTO(long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 방이 없습니다."));
        return new RoomDTO(room);
    }

    @Transactional
    @Override
    public int updateStatusIfPending(long roomId, long userId, Room.Status newStatus) {
        return roomRepository.updateStatusIfPending(roomId, userId, newStatus);
    }

    @Transactional
    @Override
    public int deleteIfPending(long roomId) {
        int deletedCount = roomRepository.deleteIfPending(roomId); // Room 삭제 시도
        if (deletedCount > 0) {
            // Room이 실제로 삭제된 경우만 파일/메시지 삭제
            List<RoomFileDTO> roomFileDTOList = roomFileRepository.findByRoomId(roomId).stream().map(RoomFileDTO::new).toList();
            for (RoomFileDTO roomFileDTO : roomFileDTOList) {
                roomFileHandler.removeFile(roomFileDTO);
            }
            roomFileRepository.deleteByRoomId(roomId);
            messageRepository.deleteByRoomId(roomId);
        }
        return deletedCount;
    }

    @Transactional
    @Override
    public void deleteRoom(long roomId) {
        List<RoomFileDTO> roomFileDTOList = roomFileRepository.findByRoomId(roomId).stream().map(RoomFileDTO::new).toList();
        for (RoomFileDTO roomFileDTO : roomFileDTOList){
            roomFileHandler.removeFile(roomFileDTO);
        }
        roomFileRepository.deleteByRoomId(roomId);
        messageRepository.deleteByRoomId(roomId);
        roomRepository.deleteById(roomId);
    }

    @Override
    public Page<RoomDTO> getMyQuizList(long userId, Room.Status status, String subject, LocalDate startDate, LocalDate endDate, Pageable sortedPageable) {
        Integer subjectId = subjectRepository.findByName(subject)
                .map(Subject::getSubjectId)
                .orElse(null);

        LocalDateTime startDateTime = null;
        LocalDateTime endDatePlus = null;

        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }

        if (endDate != null) {
            endDatePlus = endDate.plusDays(1).atStartOfDay();
        }

        Page<Room> rooms = roomRepository.findByFilters(userId, status, subjectId, startDateTime, endDatePlus, sortedPageable);

        return rooms.map(RoomDTO::new);
    }
}
