package com.StudyLink.www.repository;

import com.StudyLink.www.entity.ScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRecordRepository extends JpaRepository<ScoreRecord, Long> {
    List<ScoreRecord> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<ScoreRecord> findByUser_UserIdOrderByCreatedAtAsc(Long userId);
}
