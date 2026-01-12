package com.StudyLink.www.repository;

import com.StudyLink.www.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    // ✅ findByUser_user_id() → findByUser_UserId()
    Optional<StudentProfile> findByUser_UserId(Long userId);

    // ✅ existsByUser_user_id() → existsByUser_UserId()
    boolean existsByUser_UserId(Long userId);
}
