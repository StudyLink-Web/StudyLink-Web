package com.StudyLink.www.entity;

import com.StudyLink.www.dto.RoomDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "rooms")
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false)
    private Long studentId;

    @Column
    private Long mentorId;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer point;

    @Column(nullable = false)
    private Integer subjectId;

    @Column
    private Integer rating;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    public Room(RoomDTO dto) {
        this.roomId = dto.getRoomId();
        this.studentId = dto.getStudentId();
        this.mentorId = dto.getMentorId();
        this.createdAt = dto.getCreatedAt();
        this.isPublic = dto.getIsPublic();
        this.status = Status.valueOf(dto.getStatus().name());
        this.point = dto.getPoint();
        this.subjectId = dto.getSubjectId();
        this.rating = dto.getRating();
    }
}

