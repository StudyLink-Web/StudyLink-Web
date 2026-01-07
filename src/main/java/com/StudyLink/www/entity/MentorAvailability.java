package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;  // ✅ availability_id → availabilityId

    @Column(name = "user_id", nullable = false)
    private Long mentorId;  // ✅ user_id → mentorId (필드명도 의미있게)

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;  // ✅ String day_of_week → Integer dayOfWeek

    @Column(name = "block", nullable = false)
    private Integer block;  // ✅ start_time, end_time → block (0~11)

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // ✅ created_at → createdAt
}
