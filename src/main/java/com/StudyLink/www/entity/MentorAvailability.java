package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mentor_Availability (멘토 활동 가능 시간)
 * 요일별, 시간별 상세하게 설정 가능
 */
@Entity
@Table(name = "mentor_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avail_id")
    private Long availId;

    /**
     * 멘토 ID
     * FK: Users.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Users mentor;


    /**
     * 요일
     * 0: 일요일
     * 1: 월요일
     * 2: 화요일
     * 3: 수요일
     * 4: 목요일
     * 5: 금요일
     * 6: 토요일
     */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    /**
     * 시간 블록 (0~11)
     * 0: 00:00~02:00
     * 1: 02:00~04:00
     * 2: 04:00~06:00
     * 3: 06:00~08:00
     * 4: 08:00~10:00
     * 5: 10:00~12:00
     * 6: 12:00~14:00
     * 7: 14:00~16:00
     * 8: 16:00~18:00
     * 9: 18:00~20:00
     * 10: 20:00~22:00
     * 11: 22:00~24:00
     */
    @Column(name = "block", nullable = false)
    private Integer block;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
