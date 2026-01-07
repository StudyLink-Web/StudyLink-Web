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

    private final FavoriteRepository favoriteRepository;  // ✅ favorite_repository → favoriteRepository
    private final UserRepository userRepository;          // ✅ user_repository → userRepository

    @Transactional
    public Favorite addFavorite(Long studentId, Long mentorId) {  // ✅ student_user_id → studentId
        Users student = userRepository.findById(studentId)       // ✅
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        Users mentor = userRepository.findById(mentorId)         // ✅
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        if (!student.getRole().equals("STUDENT")) {
            throw new IllegalArgumentException("학생 역할만 멘토를 즐겨찾기할 수 있습니다.");
        }

        if (!mentor.getRole().equals("MENTOR")) {
            throw new IllegalArgumentException("멘토 역할의 사용자만 즐겨찾기할 수 있습니다.");
        }

        // ✅ 메서드명 수정
        if (favoriteRepository.existsByStudentIdAndMentorId(studentId, mentorId)) {
            throw new IllegalArgumentException("이미 즐겨찾기한 멘토입니다.");
        }

        Favorite favorite = Favorite.builder()
                .studentId(studentId)
                .mentorId(mentorId)
                .build();

        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long studentId, Long mentorId) {  // ✅ 매개변수명 수정
        // ✅ 메서드명 수정
        if (!favoriteRepository.existsByStudentIdAndMentorId(studentId, mentorId)) {
            throw new IllegalArgumentException("즐겨찾기된 멘토를 찾을 수 없습니다.");
        }
        // ✅ 메서드명 수정
        favoriteRepository.deleteByStudentIdAndMentorId(studentId, mentorId);
    }

    @Transactional(readOnly = true)
    public List<Favorite> getFavoritesByStudent(Long studentId) {  // ✅ student_user_id → studentId
        // ✅ 메서드명 수정, 제네릭 타입 추가
        return favoriteRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long studentId, Long mentorId) {  // ✅ 매개변수명 수정
        // ✅ 메서드명 수정
        return favoriteRepository.existsByStudentIdAndMentorId(studentId, mentorId);
    }
}
