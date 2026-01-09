package com.StudyLink.www.service;

import com.StudyLink.www.entity.Favorite;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.FavoriteRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    /**
     * 멘토 즐겨찾기 추가
     */
    @Transactional
    public Favorite addFavorite(Long studentId, Long mentorId) {
        // 1. 학생 사용자 확인
        Users student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 2. 멘토 사용자 확인
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        // 3. 이미 즐겨찾기되었는지 확인
        if (favoriteRepository.existsByStudentAndMentor(student, mentor)) {
            throw new IllegalArgumentException("이미 즐겨찾기한 멘토입니다.");
        }

        // 4. 자신을 즐겨찾기할 수 없음
        if (studentId.equals(mentorId)) {
            throw new IllegalArgumentException("자신을 즐겨찾기할 수 없습니다.");
        }

        // 5. 즐겨찾기 생성 및 저장
        Favorite favorite = Favorite.builder()
                .student(student)
                .mentor(mentor)
                .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("즐겨찾기 추가: 학생 ID={}, 멘토 ID={}", studentId, mentorId);

        return savedFavorite;
    }

    /**
     * 멘토 즐겨찾기 제거 (favoriteId로)
     * ✅ 수정됨: favoriteId 하나만 필요
     */
    @Transactional
    public void removeFavorite(Long favoriteId) {
        // 1. 즐겨찾기 항목 확인
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기를 찾을 수 없습니다."));

        // 2. 즐겨찾기 삭제
        favoriteRepository.delete(favorite);
        log.info("즐겨찾기 제거: 즐겨찾기 ID={}", favoriteId);
    }

    /**
     * 멘토 즐겨찾기 제거 (studentId, mentorId로)
     * ✅ 추가 메서드: 다른 방식의 삭제
     */
    @Transactional
    public void removeFavoriteByIds(Long studentId, Long mentorId) {
        Users student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        favoriteRepository.deleteByStudentAndMentor(student, mentor);
        log.info("즐겨찾기 제거: 학생 ID={}, 멘토 ID={}", studentId, mentorId);
    }

    /**
     * 학생의 즐겨찾기 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Favorite> getFavoritesByStudent(Long studentId) {
        Users student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        return favoriteRepository.findByStudent(student);
    }

    /**
     * 멘토의 팬 수 조회
     */
    @Transactional(readOnly = true)
    public long getFavoriteCountByMentor(Long mentorId) {
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        return favoriteRepository.countByMentor(mentor);
    }

    /**
     * 특정 학생이 특정 멘토를 즐겨찾기했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isFavored(Long studentId, Long mentorId) {
        Users student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        return favoriteRepository.existsByStudentAndMentor(student, mentor);
    }
}
