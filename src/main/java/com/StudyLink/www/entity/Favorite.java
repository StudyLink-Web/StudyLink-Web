package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "mentor_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;        // ✅ favorite_id (DB) → favoriteId (Java)

    @Column(name = "student_id", nullable = false)
    private Long studentId;         // ✅ student_id (DB) → studentId (Java)

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;          // ✅ mentor_id (DB) → mentorId (Java)

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // ✅ created_at (DB) → createdAt (Java)
}
