package com.StudyLink.www.init;

import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRoom {
    private final RoomService roomService;
    private final Random random = new Random();

    // 포인트 후보
    private final List<Integer> points = List.of(500, 1000, 1500);

    public void createRandomRoom() {
        // 방 생성 (예: studentId = 106)
        RoomDTO roomDTO = roomService.createRoom(106L);

        // subjectId: 1 ~ 21
        int subjectId = random.nextInt(21) + 1;
        roomDTO.setSubjectDTO(
                SubjectDTO.builder()
                        .subjectId(subjectId)
                        .build()
        );

        // mentorId: null 또는 5
        Long mentorId = random.nextBoolean() ? 105L : null;
        roomDTO.setMentorId(mentorId);
        if (mentorId != null) {
            roomDTO.setIsPublic(false);
        }

        // 상태
        roomDTO.setStatus(RoomDTO.Status.PENDING);

        // 포인트: 500 / 1000 / 1500
        int point = points.get(random.nextInt(points.size()));
        roomDTO.setPoint(point);
        roomService.save(roomDTO);
    }
}
