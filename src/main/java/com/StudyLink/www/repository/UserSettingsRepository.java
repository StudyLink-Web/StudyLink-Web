package com.StudyLink.www.repository;

import com.StudyLink.www.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    /**
     * User ID로 설정 조회
     */
    Optional<UserSettings> findByUser_UserId(Long userId);

    /**
     * User ID로 설정 존재 여부 확인
     */
    boolean existsByUser_UserId(Long userId);
}
