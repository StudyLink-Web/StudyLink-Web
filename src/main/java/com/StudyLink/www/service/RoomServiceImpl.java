package com.StudyLink.www.service;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.RoomFileDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Room;
import com.StudyLink.www.entity.StudentProfile;
import com.StudyLink.www.entity.Subject;
import com.StudyLink.www.handler.RoomFileHandler;
import com.StudyLink.www.repository.*;
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
    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final NotificationService notificationService;

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
        return roomRepository.updateStatusIfPending(roomId, userId, newStatus, LocalDateTime.now());
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

    @Transactional
    @Override
    public void deleteExpiredRooms() {
        List<Room> expiredRooms = roomRepository.findExpiredRooms();
        // 삭제전 처리해야할 로직 작성
        if (!expiredRooms.isEmpty()) {
            for (Room room : expiredRooms) {
                try {
                    StudentProfile studentProfile = studentProfileRepository.findById(room.getStudentId())
                            .orElseThrow(() -> new EntityNotFoundException("삭제할 방의 학생 프로필이 없습니다."));
                    // 학생 포인트 환불
                    studentProfile.setChargedPoint(studentProfile.getChargedPoint() + room.getPoint());


                    MentorProfile mentorProfile = mentorProfileRepository.findById(room.getMentorId())
                            .orElseThrow(() -> new EntityNotFoundException("삭제할 방의 멘토 프로필이 없습니다."));

                    // 알림
                    notificationService.createNotification(room.getStudentId(), "ROOM_EXPIRED", "멘토가 문제풀이에 실패하였습니다.", null);
                    notificationService.createNotification(room.getMentorId(), "ROOM_EXPIRED", "문제풀이에 실패하였습니다.", null);
                } catch (EntityNotFoundException e) {
                    System.out.println("프로필 없음, 방 ID: " + room.getRoomId() + " - " + e.getMessage());
                }
            }
        }
        roomRepository.deleteExpiredRooms();
    }

    @Transactional
    @Override
    public void deleteOldTempAndPendingRooms() {
        List<Room> expiredRooms = roomRepository.findOldTempAndPendingRooms();
        // 삭제전 처리해야할 로직 작성
        if (!expiredRooms.isEmpty()) {
            for (Room room : expiredRooms) {
                if (room.getStatus() == Room.Status.PENDING) {
                    try {
                        StudentProfile studentProfile = studentProfileRepository.findById(room.getStudentId())
                                .orElseThrow(() -> new EntityNotFoundException("삭제할 방의 학생 프로필이 없습니다."));
                        // 학생 포인트 환불
                        studentProfile.setChargedPoint(studentProfile.getChargedPoint() + room.getPoint());

                        // 알림
                        notificationService.createNotification(room.getStudentId(), "ROOM_EXPIRED", "장시간 미답변인 문제가 삭제되었습니다.", null);
                    } catch (EntityNotFoundException e) {
                        System.out.println("프로필 없음, 방 ID: " + room.getRoomId() + " - " + e.getMessage());
                    }
                }
            }
        }
        roomRepository.deleteOldTempAndPendingRooms();
    }
}
