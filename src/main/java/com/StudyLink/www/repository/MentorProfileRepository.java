package com.StudyLink.www.repository;

import com.StudyLink.www.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
