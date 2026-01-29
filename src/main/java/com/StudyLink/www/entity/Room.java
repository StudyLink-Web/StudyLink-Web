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

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isPublic;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer point;

    @ManyToOne(fetch = FetchType.LAZY) // ManyToOne 관계
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column
    private Integer rating;

    public enum Status {
        TEMP("임시"),
        PENDING("대기 중"),
        IN_PROGRESS("답변 중 "),
        ANSWERED("답변 완료"),
        COMPLETED("종료");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    @PrePersist
    protected void onCreate() {
        if (isPublic == null) isPublic = true;
        if (point == null) point = 0;
        if (status == null) status = Status.TEMP;
    }


    public Room(RoomDTO dto) {
        this.roomId = dto.getRoomId();
        this.studentId = dto.getStudentId();
        this.mentorId = dto.getMentorId();
        this.createdAt = dto.getCreatedAt();
        this.isPublic = dto.getIsPublic();
        this.status = Status.valueOf(dto.getStatus().name());
        this.point = dto.getPoint();
        this.subject = new Subject(dto.getSubjectDTO());
        this.rating = dto.getRating();
    }
}

