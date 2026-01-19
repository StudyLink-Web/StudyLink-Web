package com.StudyLink.www.service;

import com.StudyLink.www.dto.StudentScoreDTO;
import com.StudyLink.www.entity.StudentScore;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.StudentScoreRepository;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.repository.ScoreRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentScoreService {

    private final StudentScoreRepository studentScoreRepository;
    private final UserRepository userRepository;
    private final ScoreRecordRepository scoreRecordRepository;

    /**
     * 특정 사용자의 모든 성적 조회
     */
    @Transactional(readOnly = true)
    public List<StudentScoreDTO> getScoresByUserId(Long userId) {
        List<StudentScore> scores = studentScoreRepository.findByUser_UserIdAndScoreRecordIsNull(userId);
        return scores.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 성적 리스트 저장 (기존 성적 삭제 후 일괄 저장)
     */
    @Transactional
    public int saveScores(Long userId, List<StudentScoreDTO> scoreDTOs) {
        log.info("[StudentScoreService] Saving scores for userId: {}. Input count: {}", userId, scoreDTOs != null ? scoreDTOs.size() : 0);
        
        if (scoreDTOs == null || scoreDTOs.isEmpty()) {
            log.warn("전송된 성적 데이터가 비어있습니다. userId: {}", userId);
            return 0;
        }

        // 디버깅: 첫 번째 데이터의 상세 내용 출력
        log.info("[Debug] First Item Mapping Check: {}", scoreDTOs.get(0));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 유효한 데이터만 필터링 (과목명 필수)
        List<StudentScore> newScores = scoreDTOs.stream()
                .filter(dto -> {
                    boolean isValid = dto.getSubjectName() != null && !dto.getSubjectName().trim().isEmpty();
                    if (!isValid) log.warn("[Skip] Mapping failure or missing name: {}", dto);
                    return isValid;
                })
                .map(dto -> StudentScore.builder()
                        .user(user)
                        .subjectName(dto.getSubjectName())
                        .score(dto.getScore())
                        .scoreType(dto.getScoreType())
                        .category(dto.getCategory())
                        .optionalSubject(dto.getOptionalSubject())
                        .build())
                .collect(Collectors.toList());

        if (newScores.isEmpty()) {
            log.warn("저장 가능한 유효한 성적 데이터가 0건입니다. 필드 매핑이 실패했을 가능성이 큽니다.");
            return 0;
        }

        // 기존 활성 성적만 삭제 (이력 데이터는 보존)
        List<StudentScore> existingScores = studentScoreRepository.findByUser_UserIdAndScoreRecordIsNull(userId);
        studentScoreRepository.deleteAll(existingScores);
        studentScoreRepository.flush(); // 즉시 반영

        // 새 성적 저장
        studentScoreRepository.saveAll(newScores);
        log.info("성공적으로 {}건의 성적을 저장했습니다. userId: {}", newScores.size(), userId);
        return newScores.size();
    }

    /**
     * 제목과 함께 성적 레코드 저장
     */
    @Transactional
    public Long saveScoreRecord(Long userId, String title, List<StudentScoreDTO> scoreDTOs) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        com.StudyLink.www.entity.ScoreRecord record = com.StudyLink.www.entity.ScoreRecord.builder()
                .user(user)
                .title(title)
                .build();

        List<StudentScore> scores = scoreDTOs.stream()
                .filter(dto -> dto.getSubjectName() != null && !dto.getSubjectName().trim().isEmpty())
                .map(dto -> StudentScore.builder()
                        .user(user)
                        .subjectName(dto.getSubjectName())
                        .score(dto.getScore())
                        .scoreType(dto.getScoreType())
                        .category(dto.getCategory())
                        .optionalSubject(dto.getOptionalSubject())
                        .scoreRecord(record)
                        .build())
                .collect(Collectors.toList());

        record.setScores(scores);
        com.StudyLink.www.entity.ScoreRecord saved = scoreRecordRepository.save(record);
        return saved.getRecordId();
    }

    /**
     * 사용자의 모든 성적 레코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getScoreRecords(Long userId) {
        return scoreRecordRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(r -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.getRecordId());
                    map.put("title", r.getTitle());
                    map.put("createdAt", r.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 레코드의 상세 성적 조회
     */
    @Transactional(readOnly = true)
    public List<StudentScoreDTO> getRecordDetails(Long recordId) {
        return studentScoreRepository.findByScoreRecord_RecordId(recordId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 성적 레코드 삭제
     */
    @Transactional
    public void deleteScoreRecord(Long recordId) {
        // CascadeType.ALL에 의해 연관된 StudentScore들도 자동 삭제됨
        scoreRecordRepository.deleteById(recordId);
        log.info("[StudentScoreService] Deleted ScoreRecord ID: {}", recordId);
    }

    private StudentScoreDTO convertToDTO(StudentScore score) {
        return StudentScoreDTO.builder()
                .scoreId(score.getScoreId())
                .subjectName(score.getSubjectName())
                .score(score.getScore())
                .scoreType(score.getScoreType())
                .category(score.getCategory())
                .optionalSubject(score.getOptionalSubject())
                .build();
    }

    /**
     * 추이 분석을 위해 모든 성적 레코드와 상세 점수를 함께 조회
     */
    @Transactional(readOnly = true)
    public List<com.StudyLink.www.dto.DashboardDTO.TrendItem> getAllTrendData(Long userId) {
        return scoreRecordRepository.findByUser_UserIdOrderByTitleAsc(userId).stream()
                .map(r -> com.StudyLink.www.dto.DashboardDTO.TrendItem.builder()
                        .examName(r.getTitle())
                        .date(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                        .userScores(r.getScores().stream().map(this::convertToDTO).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
