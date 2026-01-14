package com.StudyLink.www.service;

import com.StudyLink.www.dto.StudentScoreDTO;
import com.StudyLink.www.entity.StudentScore;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.StudentScoreRepository;
import com.StudyLink.www.repository.UserRepository;
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

    /**
     * 특정 사용자의 모든 성적 조회
     */
    @Transactional(readOnly = true)
    public List<StudentScoreDTO> getScoresByUserId(Long userId) {
        List<StudentScore> scores = studentScoreRepository.findByUser_UserId(userId);
        return scores.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 성적 리스트 저장 (기존 성적 삭제 후 일괄 저장)
     */
    @Transactional
    public void saveScores(Long userId, List<StudentScoreDTO> scoreDTOs) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 성적 삭제
        List<StudentScore> existingScores = studentScoreRepository.findByUser_UserId(userId);
        studentScoreRepository.deleteAll(existingScores);

        // 새 성적 저장 (과목명이 없는 데이터는 걸러냄)
        List<StudentScore> newScores = scoreDTOs.stream()
                .filter(dto -> dto.getSubjectName() != null && !dto.getSubjectName().trim().isEmpty()) // 방어 로직 추가
                .map(dto -> StudentScore.builder()
                        .user(user)
                        .subjectName(dto.getSubjectName())
                        .score(dto.getScore())
                        .scoreType(dto.getScoreType())
                        .category(dto.getCategory())
                        .build())
                .collect(Collectors.toList());

        if (newScores.isEmpty()) {
            log.warn("⚠️ 저장할 유효한 성적 데이터가 없습니다. (userId: {})", userId);
            return;
        }

        studentScoreRepository.saveAll(newScores);
        log.info("✅ 사용자의 성적이 저장되었습니다: userId={}, subjects={}", 
                userId, newScores.stream().map(StudentScore::getSubjectName).collect(Collectors.toList()));
    }

    private StudentScoreDTO convertToDTO(StudentScore score) {
        return StudentScoreDTO.builder()
                .scoreId(score.getScoreId())
                .subjectName(score.getSubjectName())
                .score(score.getScore())
                .scoreType(score.getScoreType())
                .category(score.getCategory())
                .build();
    }
}
