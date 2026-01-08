package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Room;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {
    private Long roomId;
    private Long studentId;
    private Long mentorId;
    private LocalDateTime createdAt;
    private Boolean isPublic;
    private Status status;
    private Integer point;
    private Integer subjectId;
    private Integer rating;

    public enum Status {
        TEMP,
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    public RoomDTO(Room room) {
        this.roomId = room.getRoomId();
        this.studentId = room.getStudentId();
        this.mentorId = room.getMentorId();
        this.createdAt = room.getCreatedAt();
        this.isPublic = room.getIsPublic();
        this.status = Status.valueOf(room.getStatus().name()); // Enum 매핑
        this.point = room.getPoint();
        this.subjectId = room.getSubjectId();
        this.rating = room.getRating();
    }
}
