package com.StudyLink.www.repository;

import com.StudyLink.www.entity.StudentScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentScoreRepository extends JpaRepository<StudentScore, Long> {
    
    // 특정 사용자의 모든 성적 리스트 조회
    List<StudentScore> findByUser_UserId(Long userId);

    // 특정 성적 레코드에 포함된 모든 성적 조회
    List<StudentScore> findByScoreRecord_RecordId(Long recordId);
}
