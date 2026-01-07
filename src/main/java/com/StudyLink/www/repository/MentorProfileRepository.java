package com.StudyLink.www.repository;

import com.StudyLink.www.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    // ✅ User 필드가 있으므로 userId로 검색
    boolean existsByUser_UserId(Long userId);

    // ✅ User 필드가 있으므로 userId로 검색
    Optional<MentorProfile> findByUser_UserId(Long userId);
}
