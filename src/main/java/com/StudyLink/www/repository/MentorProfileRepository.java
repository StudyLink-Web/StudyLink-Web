package com.StudyLink.www.repository;

import com.StudyLink.www.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    /**
     * User ID로 멘토 프로필 존재 여부 확인
     */
    boolean existsByUser_UserId(Long userId);

    /**
     * User ID로 멘토 프로필 조회
     */
    Optional<MentorProfile> findByUser_UserId(Long userId);

    /**
     * 인증된 모든 멘토 조회
     * ✅ 추가됨
     */
    List<MentorProfile> findByIsVerifiedTrue();

    /**
     * 미인증 멘토 조회 (선택사항)
     */
    List<MentorProfile> findByIsVerifiedFalse();


    /**
     * 멘토의 수업 횟수 조회
     * Lesson 테이블에서 mentor_id와 status가 'completed'인 개수 계산
     */
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.mentor.userId = :mentorId AND l.status = 'completed'")
    long countLessonsByMentorId(@Param("mentorId") Long mentorId);

    /**
     * 멘토의 리뷰 개수 조회
     * Review 테이블에서 mentor_id인 개수 계산
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.mentor.userId = :mentorId")
    long countReviewsByMentorId(@Param("mentorId") Long mentorId);
}
