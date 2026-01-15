package com.StudyLink.www.entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "score_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "scoreRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentScore> scores = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
