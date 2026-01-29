package com.StudyLink.www.repository;

import com.StudyLink.www.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);
}
